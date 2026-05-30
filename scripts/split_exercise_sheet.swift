import AppKit
import CoreGraphics
import Foundation
import ImageIO

struct Config {
    let inputPath: String
    let outputDirectory: String
    let outputPrefix: String
    let panelCount: Int
    let targetWidth: Int
    let targetHeight: Int
}

func fail(_ message: String) -> Never {
    FileHandle.standardError.write(Data((message + "\n").utf8))
    exit(1)
}

let args = CommandLine.arguments
guard args.count == 5 || args.count == 7 else {
    fail("Usage: swift scripts/split_exercise_sheet.swift <input.png> <output-dir> <output-prefix> <panel-count> [target-width target-height]")
}

let config = Config(
    inputPath: args[1],
    outputDirectory: args[2],
    outputPrefix: args[3],
    panelCount: Int(args[4]) ?? 0,
    targetWidth: args.count == 7 ? (Int(args[5]) ?? 0) : 720,
    targetHeight: args.count == 7 ? (Int(args[6]) ?? 0) : 800
)

guard config.panelCount > 0, config.targetWidth > 0, config.targetHeight > 0 else {
    fail("Panel count and target dimensions must be positive integers.")
}

guard let image = NSImage(contentsOfFile: config.inputPath) else {
    fail("Cannot read image: \(config.inputPath)")
}

var proposedRect = CGRect(origin: .zero, size: image.size)
guard let source = image.cgImage(forProposedRect: &proposedRect, context: nil, hints: nil) else {
    fail("Cannot decode image: \(config.inputPath)")
}

try FileManager.default.createDirectory(
    atPath: config.outputDirectory,
    withIntermediateDirectories: true
)

let sourceWidth = source.width
let sourceHeight = source.height
let scaleColor = CGColor(red: 0.973, green: 0.953, blue: 0.910, alpha: 1.0)

for index in 0..<config.panelCount {
    let startX = Int((Double(sourceWidth) * Double(index) / Double(config.panelCount)).rounded(.down))
    let endX = Int((Double(sourceWidth) * Double(index + 1) / Double(config.panelCount)).rounded(.down))
    let cropRect = CGRect(x: startX, y: 0, width: max(1, endX - startX), height: sourceHeight)

    guard let cropped = source.cropping(to: cropRect) else {
        fail("Cannot crop panel \(index + 1)")
    }

    guard let context = CGContext(
        data: nil,
        width: config.targetWidth,
        height: config.targetHeight,
        bitsPerComponent: 8,
        bytesPerRow: 0,
        space: CGColorSpaceCreateDeviceRGB(),
        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    ) else {
        fail("Cannot create output context.")
    }

    context.setFillColor(scaleColor)
    context.fill(CGRect(x: 0, y: 0, width: config.targetWidth, height: config.targetHeight))
    context.interpolationQuality = .high

    let scale = min(
        Double(config.targetWidth) / Double(cropped.width),
        Double(config.targetHeight) / Double(cropped.height)
    )
    let drawWidth = Double(cropped.width) * scale
    let drawHeight = Double(cropped.height) * scale
    let drawX = (Double(config.targetWidth) - drawWidth) / 2.0
    let drawY = (Double(config.targetHeight) - drawHeight) / 2.0
    let drawRect = CGRect(x: drawX, y: drawY, width: drawWidth, height: drawHeight)

    context.draw(cropped, in: drawRect)

    guard let outputImage = context.makeImage(),
          let destination = CGImageDestinationCreateWithURL(
              URL(fileURLWithPath: "\(config.outputDirectory)/\(config.outputPrefix)_step_\(index + 1).png") as CFURL,
              "public.png" as CFString,
              1,
              nil
          ) else {
        fail("Cannot prepare output for panel \(index + 1)")
    }

    CGImageDestinationAddImage(destination, outputImage, nil)
    if !CGImageDestinationFinalize(destination) {
        fail("Cannot write panel \(index + 1)")
    }
}
