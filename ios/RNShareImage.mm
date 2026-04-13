#import "RNShareImage.h"
#import <React/RCTUtils.h>
#import <React/RCTBridge.h>
#import <React/UIView+React.h>
#import <UIKit/UIKit.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNShareImageSpec/RNShareImageSpec.h>
#import <React/RCTViewComponentView.h>
#endif

static NSString *const kErrorNoWindow = @"E_NO_WINDOW";
static NSString *const kErrorNoViewController = @"E_NO_VIEW_CONTROLLER";
static NSString *const kErrorViewNotFound = @"E_VIEW_NOT_FOUND";
static NSString *const kErrorScreenshotFailed = @"E_SCREENSHOT_FAILED";
static NSString *const kErrorDownloadFailed = @"E_DOWNLOAD_FAILED";
static NSString *const kErrorInvalidUrl = @"E_INVALID_URL";

@implementation RNShareImage

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

#pragma mark - Window Access (iOS 13+ Scene Support)

- (UIWindow *)getKeyWindow
{
    if (@available(iOS 13.0, *)) {
        for (UIWindowScene *scene in UIApplication.sharedApplication.connectedScenes) {
            if (scene.activationState == UISceneActivationStateForegroundActive) {
                for (UIWindow *window in scene.windows) {
                    if (window.isKeyWindow) {
                        return window;
                    }
                }
            }
        }
    }

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    return UIApplication.sharedApplication.keyWindow;
#pragma clang diagnostic pop
}

- (UIViewController *)getRootViewController
{
    return [self getKeyWindow].rootViewController;
}

#pragma mark - View Finding

- (UIView *)findViewWithNativeID:(NSString *)nativeID inView:(UIView *)view
{
#ifdef RCT_NEW_ARCH_ENABLED
    // New Architecture (Fabric): Check nativeId property on RCTViewComponentView
    if ([view isKindOfClass:[RCTViewComponentView class]]) {
        RCTViewComponentView *componentView = (RCTViewComponentView *)view;
        if (componentView.nativeId != nil && [componentView.nativeId isEqualToString:nativeID]) {
            return view;
        }
    }
#endif

    // Check accessibilityIdentifier
    if (view.accessibilityIdentifier != nil && [view.accessibilityIdentifier isEqualToString:nativeID]) {
        return view;
    }

    // Old Architecture: Check nativeID property from UIView+React category
    if ([view respondsToSelector:@selector(nativeID)]) {
        NSString *viewNativeID = view.nativeID;
        if (viewNativeID != nil && [viewNativeID isEqualToString:nativeID]) {
            return view;
        }
    }

    // Recursively search subviews
    for (UIView *subview in view.subviews) {
        UIView *found = [self findViewWithNativeID:nativeID inView:subview];
        if (found) {
            return found;
        }
    }

    return nil;
}

- (UIView *)findViewWithNativeID:(NSString *)nativeID
{
    // Search from the key window to cover all view hierarchies
    UIWindow *window = [self getKeyWindow];
    if (!window) {
        return nil;
    }
    return [self findViewWithNativeID:nativeID inView:window];
}

#pragma mark - Screenshot Capture

- (UIImage *)captureScreenshotOfView:(UIView *)view
{
    if (view.bounds.size.width <= 0 || view.bounds.size.height <= 0) {
        return nil;
    }

    UIGraphicsImageRenderer *renderer = [[UIGraphicsImageRenderer alloc] initWithSize:view.bounds.size];

    return [renderer imageWithActions:^(UIGraphicsImageRendererContext *rendererContext) {
        [view drawViewHierarchyInRect:view.bounds afterScreenUpdates:YES];
    }];
}

- (NSString *)saveImageToTempFile:(UIImage *)image withFilename:(NSString *)filename
{
    NSData *imageData = UIImagePNGRepresentation(image);
    if (!imageData) {
        return nil;
    }

    NSString *tempDir = NSTemporaryDirectory();
    NSString *filePath = [tempDir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.png", filename]];

    if ([imageData writeToFile:filePath atomically:YES]) {
        return filePath;
    }

    return nil;
}

#pragma mark - Image Download

- (void)downloadImageFromURL:(NSURL *)url completion:(void (^)(UIImage *image, NSError *error))completion
{
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    config.timeoutIntervalForRequest = 30.0;
    config.timeoutIntervalForResource = 60.0;

    NSURLSession *session = [NSURLSession sessionWithConfiguration:config];

    NSURLSessionDataTask *task = [session dataTaskWithURL:url completionHandler:^(NSData *data, NSURLResponse *response, NSError *error) {
        if (error) {
            completion(nil, error);
            return;
        }

        NSHTTPURLResponse *httpResponse = (NSHTTPURLResponse *)response;
        if (httpResponse.statusCode != 200) {
            NSError *httpError = [NSError errorWithDomain:@"RNShareImage"
                                                     code:httpResponse.statusCode
                                                 userInfo:@{NSLocalizedDescriptionKey: [NSString stringWithFormat:@"HTTP error: %ld", (long)httpResponse.statusCode]}];
            completion(nil, httpError);
            return;
        }

        UIImage *image = [UIImage imageWithData:data];
        if (!image) {
            NSError *imageError = [NSError errorWithDomain:@"RNShareImage"
                                                      code:-1
                                                  userInfo:@{NSLocalizedDescriptionKey: @"Could not decode image data"}];
            completion(nil, imageError);
            return;
        }

        completion(image, nil);
    }];

    [task resume];
}

#pragma mark - Share Presentation

- (void)presentShareSheetWithItems:(NSArray *)activityItems
                    viewController:(UIViewController *)viewController
                        completion:(void (^)(void))completion
{
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:activityItems applicationActivities:nil];

    // iPad popover support
    if ([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad) {
        activityVC.popoverPresentationController.sourceView = viewController.view;
        activityVC.popoverPresentationController.sourceRect = CGRectMake(
            viewController.view.bounds.size.width / 2,
            viewController.view.bounds.size.height / 2,
            0,
            0
        );
    }

    activityVC.completionWithItemsHandler = ^(UIActivityType activityType, BOOL completed, NSArray *returnedItems, NSError *activityError) {
        if (completion) {
            completion();
        }
    };

    [viewController presentViewController:activityVC animated:YES completion:nil];
}

#pragma mark - Exported Methods

RCT_EXPORT_METHOD(shareScreenshot:(NSString *)viewId
                  message:(NSString *)message
                  filename:(NSString *)filename
                  shareTitle:(NSString *)shareTitle
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *rootViewController = [self getRootViewController];
        if (!rootViewController) {
            reject(kErrorNoViewController, @"No root view controller available", nil);
            return;
        }

        UIView *targetView = nil;

        if (viewId != nil && ![viewId isEqual:[NSNull null]] && viewId.length > 0) {
            targetView = [self findViewWithNativeID:viewId];
            if (!targetView) {
                reject(kErrorViewNotFound, [NSString stringWithFormat:@"View not found with nativeID: %@", viewId], nil);
                return;
            }
        } else {
            targetView = rootViewController.view;
        }

        UIImage *screenshot = [self captureScreenshotOfView:targetView];
        if (!screenshot) {
            reject(kErrorScreenshotFailed, @"Failed to capture screenshot - view may have invalid dimensions", nil);
            return;
        }

        NSString *imagePath = [self saveImageToTempFile:screenshot withFilename:filename];
        if (!imagePath) {
            reject(kErrorScreenshotFailed, @"Failed to save screenshot to file", nil);
            return;
        }

        NSURL *imageURL = [NSURL fileURLWithPath:imagePath];
        NSArray *activityItems = @[message, imageURL];

        [self presentShareSheetWithItems:activityItems viewController:rootViewController completion:^{
            resolve(nil);
        }];
    });
}

RCT_EXPORT_METHOD(shareImageFromUri:(NSString *)imageUri
                  message:(NSString *)message
                  shareTitle:(NSString *)shareTitle
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
    NSURL *url = [NSURL URLWithString:imageUri];
    if (!url) {
        reject(kErrorInvalidUrl, [NSString stringWithFormat:@"Invalid URL: %@", imageUri], nil);
        return;
    }

    BOOL isRemoteURL = [url.scheme.lowercaseString isEqualToString:@"http"] || [url.scheme.lowercaseString isEqualToString:@"https"];

    if (isRemoteURL) {
        [self downloadImageFromURL:url completion:^(UIImage *image, NSError *error) {
            if (error) {
                reject(kErrorDownloadFailed, [NSString stringWithFormat:@"Failed to download image: %@", error.localizedDescription], error);
                return;
            }

            // Save to temp file for better share sheet preview
            NSString *filename = [url.lastPathComponent stringByDeletingPathExtension] ?: @"shared_image";
            NSString *imagePath = [self saveImageToTempFile:image withFilename:filename];
            if (!imagePath) {
                reject(kErrorDownloadFailed, @"Failed to save downloaded image", nil);
                return;
            }

            dispatch_async(dispatch_get_main_queue(), ^{
                UIViewController *rootViewController = [self getRootViewController];
                if (!rootViewController) {
                    reject(kErrorNoViewController, @"No root view controller available", nil);
                    return;
                }

                NSURL *imageURL = [NSURL fileURLWithPath:imagePath];
                NSArray *activityItems = @[message, imageURL];
                [self presentShareSheetWithItems:activityItems viewController:rootViewController completion:^{
                    resolve(nil);
                }];
            });
        }];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            UIViewController *rootViewController = [self getRootViewController];
            if (!rootViewController) {
                reject(kErrorNoViewController, @"No root view controller available", nil);
                return;
            }

            NSArray *activityItems = @[message, url];
            [self presentShareSheetWithItems:activityItems viewController:rootViewController completion:^{
                resolve(nil);
            }];
        });
    }
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNShareImageSpecJSI>(params);
}
#endif

@end
