// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "InAppUpdatesCore",
    platforms: [
        .iOS(.v13),
        .macOS(.v12)
    ],
    products: [
        .library(name: "AppStoreLookupCore", targets: ["AppStoreLookupCore"])
    ],
    targets: [
        .target(
            name: "AppStoreLookupCore",
            path: "ios/Core"
        ),
        .testTarget(
            name: "AppStoreLookupCoreTests",
            dependencies: ["AppStoreLookupCore"],
            path: "ios/CoreTests"
        )
    ]
)
