require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version']
  s.summary        = package['summary']
  s.description    = package['description']
  s.authors        = package['author']
  s.license        = package['license']
  s.homepage       = package['homepage']
  s.platform     = :ios, "10.0"
  s.source       = { :git => "https://github.com/ToluThomas/react-native-share-image.git", :tag => "#{s.version}" }
  s.source_files  = "ios/**/*.{h,m}"
  s.requires_arc = true
  s.dependency "React-Core"
end

  