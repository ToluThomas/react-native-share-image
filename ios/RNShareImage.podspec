
Pod::Spec.new do |s|
  s.name         = "RNShareImage"
  s.version      = "1.0.0"
  s.summary      = "RNShareImage"
  s.description  = <<-DESC
                  RNShareImage
                   DESC
  s.homepage     = ""
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/author/RNShareImage.git", :tag => "master" }
  s.source_files  = "RNShareImage/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  