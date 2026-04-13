#import "RNShareImage.h"
#import <React/RCTUtils.h>
#import <React/RCTBridge.h>
#import <React/UIView+React.h>
#import <UIKit/UIKit.h>

#ifdef RCT_NEW_ARCH_ENABLED
#import <RNShareImageSpec/RNShareImageSpec.h>
#endif

@implementation RNShareImage

RCT_EXPORT_MODULE()

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

#pragma mark - Helper Methods

- (UIView *)findViewWithNativeID:(NSString *)nativeID inView:(UIView *)view
{
    if ([view.nativeID isEqualToString:nativeID]) {
        return view;
    }

    for (UIView *subview in view.subviews) {
        UIView *found = [self findViewWithNativeID:nativeID inView:subview];
        if (found) {
            return found;
        }
    }

    return nil;
}

- (UIView *)getRootView
{
    UIWindow *window = RCTKeyWindow();
    return window.rootViewController.view;
}

- (UIImage *)captureScreenshotOfView:(UIView *)view
{
    UIGraphicsImageRenderer *renderer = [[UIGraphicsImageRenderer alloc] initWithSize:view.bounds.size];

    UIImage *image = [renderer imageWithActions:^(UIGraphicsImageRendererContext *rendererContext) {
        [view drawViewHierarchyInRect:view.bounds afterScreenUpdates:YES];
    }];

    return image;
}

- (NSString *)saveImageToTempFile:(UIImage *)image withFilename:(NSString *)filename
{
    NSData *imageData = UIImagePNGRepresentation(image);
    NSString *tempDir = NSTemporaryDirectory();
    NSString *filePath = [tempDir stringByAppendingPathComponent:[NSString stringWithFormat:@"%@.png", filename]];

    [imageData writeToFile:filePath atomically:YES];

    return filePath;
}

- (void)shareImageAtPath:(NSString *)imagePath withMessage:(NSString *)message andTitle:(NSString *)title
{
    NSURL *imageURL = [NSURL fileURLWithPath:imagePath];

    NSArray *activityItems = @[message, imageURL];
    UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:activityItems applicationActivities:nil];

    activityVC.excludedActivityTypes = @[];

    // For iPad support
    UIViewController *rootViewController = RCTKeyWindow().rootViewController;
    if ([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad) {
        activityVC.popoverPresentationController.sourceView = rootViewController.view;
        activityVC.popoverPresentationController.sourceRect = CGRectMake(
            rootViewController.view.bounds.size.width / 2,
            rootViewController.view.bounds.size.height / 2,
            0,
            0
        );
    }

    [rootViewController presentViewController:activityVC animated:YES completion:nil];
}

#pragma mark - Exported Methods

RCT_EXPORT_METHOD(shareScreenshot:(NSString *)viewId
                  message:(NSString *)message
                  filename:(NSString *)filename
                  shareTitle:(NSString *)shareTitle)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            UIView *targetView;

            if (viewId != nil && ![viewId isEqual:[NSNull null]] && viewId.length > 0) {
                targetView = [self findViewWithNativeID:viewId inView:[self getRootView]];
            }

            if (!targetView) {
                targetView = [self getRootView];
            }

            UIImage *screenshot = [self captureScreenshotOfView:targetView];
            NSString *imagePath = [self saveImageToTempFile:screenshot withFilename:filename];
            [self shareImageAtPath:imagePath withMessage:message andTitle:shareTitle];
        } @catch (NSException *exception) {
            NSLog(@"RNShareImage: Error capturing screenshot: %@", exception.reason);
        }
    });
}

RCT_EXPORT_METHOD(shareImageFromUri:(NSString *)imageUri
                  message:(NSString *)message
                  shareTitle:(NSString *)shareTitle)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        @try {
            NSURL *url = [NSURL URLWithString:imageUri];

            NSArray *activityItems;

            if ([url.scheme isEqualToString:@"file"]) {
                activityItems = @[message, url];
            } else {
                // For remote URLs, download the image first
                NSData *imageData = [NSData dataWithContentsOfURL:url];
                if (imageData) {
                    UIImage *image = [UIImage imageWithData:imageData];
                    activityItems = @[message, image];
                } else {
                    NSLog(@"RNShareImage: Could not load image from URI: %@", imageUri);
                    return;
                }
            }

            UIActivityViewController *activityVC = [[UIActivityViewController alloc] initWithActivityItems:activityItems applicationActivities:nil];

            UIViewController *rootViewController = RCTKeyWindow().rootViewController;
            if ([UIDevice currentDevice].userInterfaceIdiom == UIUserInterfaceIdiomPad) {
                activityVC.popoverPresentationController.sourceView = rootViewController.view;
                activityVC.popoverPresentationController.sourceRect = CGRectMake(
                    rootViewController.view.bounds.size.width / 2,
                    rootViewController.view.bounds.size.height / 2,
                    0,
                    0
                );
            }

            [rootViewController presentViewController:activityVC animated:YES completion:nil];
        } @catch (NSException *exception) {
            NSLog(@"RNShareImage: Error sharing image: %@", exception.reason);
        }
    });
}

#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:(const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeRNShareImageSpecJSI>(params);
}
#endif

@end
