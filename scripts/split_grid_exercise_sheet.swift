import CoreGraphics
import Foundation
import ImageIO

struct Config {
    let inputPath: String
    let outputDirectory: String
    let outputPrefix: String
    let columns: Int
    let rows: Int
    let targetWidth: Int
    let targetHeight: Int
    let visibleHeightRatio: Double
}

func fail(_ message: String) -> Never {
    FileHandle.standardError.write(Data((message + "\n").utf8))
    exit(1)
}

let args = CommandLine.arguments
guard args.count == 6 || args.count == 9 || args.count == 10 else {
    fail("Usage: swift scripts/split_grid_exercise_sheet.swift <input.png> <output-dir> <output-prefix> <columns> <rows> [target-width target-height visible-height-ratio]")
}

let config = Config(
    inputPath: args[1],
    outputDirectory: args[2],
    outputPrefix: args[3],
    columns: Int(args[4]) ?? 0,
    rows: Int(args[5]) ?? 0,
    targetWidth: args.count >= 9 ? (Int(args[6]) ?? 0) : 720,
    targetHeight: args.count >= 9 ? (Int(args[7]) ?? 0) : 800,
    visibleHeightRatio: args.count >= 9 ? (Double(args[8]) ?? 0.84) : 0.84
)

guard config.columns > 0,
      config.rows > 0,
      config.targetWidth > 0,
      config.targetHeight > 0,
      config.visibleHeightRatio > 0.1,
      config.visibleHeightRatio <= 1.0
else {
    fail("Grid, target dimensions, and visible-height-ratio must be valid.")
}

guard let sourceHandle = CGImageSourceCreateWithURL(URL(fileURLWithPath: config.inputPath) as CFURL, nil),
      let source = CGImageSourceCreateImageAtIndex(sourceHandle, 0, nil)
else {
    fail("Cannot decode image: \(config.inputPath)")
}

try FileManager.default.createDirectory(atPath: config.outputDirectory, withIntermediateDirectories: true)

let panelWidth = source.width / config.columns
let panelHeight = source.height / config.rows
let visibleHeight = max(1, Int((Double(panelHeight) * config.visibleHeightRatio).rounded(.down)))
let background = CGColor(red: 0.973, green: 0.953, blue: 0.910, alpha: 1.0)

for row in 0..<config.rows {
    for column in 0..<config.columns {
        let stepIndex = row * config.columns + column + 1
        let cropRect = CGRect(
            x: column * panelWidth,
            y: row * panelHeight,
            width: panelWidth,
            height: visibleHeight
        )

        guard let cropped = source.cropping(to: cropRect),
              let context = CGContext(
                  data: nil,
                  width: config.targetWidth,
                  height: config.targetHeight,
                  bitsPerComponent: 8,
                  bytesPerRow: 0,
                  space: CGColorSpaceCreateDeviceRGB(),
                  bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
              )
        else {
            fail("Cannot crop or create context for step \(stepIndex)")
        }

        context.setFillColor(background)
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

        context.draw(cropped, in: CGRect(x: drawX, y: drawY, width: drawWidth, height: drawHeight))

        let outputPath = "\(config.outputDirectory)/\(config.outputPrefix)_step_\(stepIndex).png"
        guard let image = context.makeImage(),
              let destination = CGImageDestinationCreateWithURL(
                  URL(fileURLWithPath: outputPath) as CFURL,
                  "public.png" as CFString,
                  1,
                  nil
              )
        else {
            fail("Cannot prepare output for step \(stepIndex)")
        }

        CGImageDestinationAddImage(destination, image, nil)
        if !CGImageDestinationFinalize(destination) {
            fail("Cannot write output: \(outputPath)")
        }
    }
}
