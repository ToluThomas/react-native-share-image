require 'json'

package = JSON.parse(File.read(File.join(__dir__, '../package.json')))

Pod::Spec.new do |s|
  s.name           = package['name']
  s.version        = package['version']
  s.summary        = package['summary']
  s.description    = package['description']
  s.authors        = package['author']
  s.license        = package['license']
  s.homepage       = package['homepage']
  s.platforms      = { :ios => "15.1" }
  s.source         = { :git => package['repository']['url'], :tag => "#{s.version}" }
  s.source_files   = "*.{h,m,mm}"
  s.requires_arc   = true

  install_modules_dependencies(s)
end
