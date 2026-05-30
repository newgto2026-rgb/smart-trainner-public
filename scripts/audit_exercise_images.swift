#!/usr/bin/env swift
import AppKit
import Foundation

struct Config {
    var repoRoot = URL(fileURLWithPath: FileManager.default.currentDirectoryPath)
    var outDir: URL?
    var resDir: URL?
}

struct ImageStats {
    let width: Int
    let height: Int
    let averageHex: String
    let backgroundHex: String
}

struct Asset {
    let name: String
    let url: URL
    let kind: String
    let exerciseId: String
    let step: Int?
    let stats: ImageStats
    let referenced: Bool
}

let stepPattern = try! NSRegularExpression(pattern: #"^exercise_(.+)_step_(\d+)\.(png|webp)$"#)
let thumbnailPattern = try! NSRegularExpression(pattern: #"^exercise_thumbnail_(.+)\.(png|webp)$"#)
let drawableRefPattern = try! NSRegularExpression(pattern: #"R\.drawable\.([A-Za-z0-9_]+)"#)

func parseArgs() -> Config {
    var config = Config()
    var args = Array(CommandLine.arguments.dropFirst())
    while !args.isEmpty {
        let key = args.removeFirst()
        guard !args.isEmpty else { continue }
        let value = args.removeFirst()
        switch key {
        case "--repo-root":
            config.repoRoot = URL(fileURLWithPath: value)
        case "--out-dir":
            config.outDir = URL(fileURLWithPath: value)
        case "--res-dir":
            config.resDir = URL(fileURLWithPath: value)
        default:
            continue
        }
    }
    return config
}

func relativePath(_ url: URL, from root: URL) -> String {
    let rootPath = root.standardizedFileURL.path
    let path = url.standardizedFileURL.path
    if path.hasPrefix(rootPath + "/") {
        return String(path.dropFirst(rootPath.count + 1))
    }
    return path
}

func match(_ regex: NSRegularExpression, _ text: String) -> NSTextCheckingResult? {
    let range = NSRange(text.startIndex..<text.endIndex, in: text)
    return regex.firstMatch(in: text, range: range)
}

func capture(_ result: NSTextCheckingResult, _ index: Int, in text: String) -> String {
    String(text[Range(result.range(at: index), in: text)!])
}

func loadCGImage(_ url: URL) throws -> CGImage {
    guard let image = NSImage(contentsOf: url) else {
        throw NSError(domain: "ExerciseImageAudit", code: 1, userInfo: [NSLocalizedDescriptionKey: "Cannot load image: \(url.path)"])
    }
    var rect = NSRect(origin: .zero, size: image.size)
    guard let cgImage = image.cgImage(forProposedRect: &rect, context: nil, hints: nil) else {
        throw NSError(domain: "ExerciseImageAudit", code: 2, userInfo: [NSLocalizedDescriptionKey: "Cannot decode image: \(url.path)"])
    }
    return cgImage
}

func rgbaPixels(for cgImage: CGImage) throws -> (width: Int, height: Int, data: [UInt8]) {
    let width = cgImage.width
    let height = cgImage.height
    let bytesPerPixel = 4
    let bytesPerRow = width * bytesPerPixel
    var data = [UInt8](repeating: 0, count: height * bytesPerRow)
    let colorSpace = CGColorSpaceCreateDeviceRGB()
    guard let context = CGContext(
        data: &data,
        width: width,
        height: height,
        bitsPerComponent: 8,
        bytesPerRow: bytesPerRow,
        space: colorSpace,
        bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue
    ) else {
        throw NSError(domain: "ExerciseImageAudit", code: 3, userInfo: [NSLocalizedDescriptionKey: "Cannot create bitmap context"])
    }
    context.draw(cgImage, in: CGRect(x: 0, y: 0, width: width, height: height))
    return (width, height, data)
}

func hexColor(r: Double, g: Double, b: Double) -> String {
    String(format: "#%02X%02X%02X", Int(r.rounded()), Int(g.rounded()), Int(b.rounded()))
}

func stats(for url: URL) throws -> ImageStats {
    let cgImage = try loadCGImage(url)
    let pixels = try rgbaPixels(for: cgImage)
    var totalR = 0.0
    var totalG = 0.0
    var totalB = 0.0
    var totalA = 0.0
    var edgeR = 0.0
    var edgeG = 0.0
    var edgeB = 0.0
    var edgeA = 0.0
    let edgeWidth = max(1, min(pixels.width, pixels.height) / 12)
    let sampleStride = max(1, min(pixels.width, pixels.height) / 160)

    for y in stride(from: 0, to: pixels.height, by: sampleStride) {
        for x in stride(from: 0, to: pixels.width, by: sampleStride) {
            let offset = (y * pixels.width + x) * 4
            let alpha = Double(pixels.data[offset + 3]) / 255.0
            let red = Double(pixels.data[offset])
            let green = Double(pixels.data[offset + 1])
            let blue = Double(pixels.data[offset + 2])
            totalR += red * alpha
            totalG += green * alpha
            totalB += blue * alpha
            totalA += alpha

            if x < edgeWidth || y < edgeWidth || x >= pixels.width - edgeWidth || y >= pixels.height - edgeWidth {
                edgeR += red * alpha
                edgeG += green * alpha
                edgeB += blue * alpha
                edgeA += alpha
            }
        }
    }

    let safeTotalA = max(totalA, 1)
    let safeEdgeA = max(edgeA, 1)
    return ImageStats(
        width: pixels.width,
        height: pixels.height,
        averageHex: hexColor(r: totalR / safeTotalA, g: totalG / safeTotalA, b: totalB / safeTotalA),
        backgroundHex: hexColor(r: edgeR / safeEdgeA, g: edgeG / safeEdgeA, b: edgeB / safeEdgeA)
    )
}

func swiftFilesUnder(_ root: URL) -> [URL] {
    guard let enumerator = FileManager.default.enumerator(
        at: root,
        includingPropertiesForKeys: [.isRegularFileKey],
        options: [.skipsHiddenFiles]
    ) else {
        return []
    }
    return enumerator.compactMap { item in
        guard let url = item as? URL else { return nil }
        if url.pathComponents.contains("build") { return nil }
        return url.pathExtension == "kt" ? url : nil
    }
}

func referencedDrawables(in roots: [URL]) -> Set<String> {
    var refs = Set<String>()
    for file in roots.flatMap(swiftFilesUnder) {
        guard let text = try? String(contentsOf: file) else { continue }
        let range = NSRange(text.startIndex..<text.endIndex, in: text)
        drawableRefPattern.enumerateMatches(in: text, range: range) { result, _, _ in
            guard let result else { return }
            refs.insert(capture(result, 1, in: text))
        }
    }
    return refs
}

func discoverAssets(resDir: URL, refs: Set<String>) throws -> [Asset] {
    let contents = try FileManager.default.contentsOfDirectory(at: resDir, includingPropertiesForKeys: nil)
    var assets: [Asset] = []
    for url in contents.sorted(by: { $0.lastPathComponent < $1.lastPathComponent }) {
        let filename = url.lastPathComponent
        let lowerExt = url.pathExtension.lowercased()
        guard lowerExt == "png" || lowerExt == "webp" else { continue }
        if let result = match(stepPattern, filename) {
            let name = url.deletingPathExtension().lastPathComponent
            assets.append(Asset(
                name: name,
                url: url,
                kind: "step",
                exerciseId: capture(result, 1, in: filename),
                step: Int(capture(result, 2, in: filename)),
                stats: try stats(for: url),
                referenced: refs.contains(name)
            ))
        } else if let result = match(thumbnailPattern, filename) {
            let name = url.deletingPathExtension().lastPathComponent
            assets.append(Asset(
                name: name,
                url: url,
                kind: "thumbnail",
                exerciseId: capture(result, 1, in: filename),
                step: nil,
                stats: try stats(for: url),
                referenced: refs.contains(name)
            ))
        }
    }
    return assets
}

func writeContactSheet(assets: [Asset], to outURL: URL) throws {
    let cellWidth = 190
    let cellHeight = 190
    let imageHeight = 142
    let columns = 6
    let rows = max(1, Int(ceil(Double(assets.count) / Double(columns))))
    let canvasSize = NSSize(width: columns * cellWidth, height: rows * cellHeight)
    let canvas = NSImage(size: canvasSize)

    canvas.lockFocus()
    NSColor(calibratedWhite: 0.98, alpha: 1).setFill()
    NSRect(origin: .zero, size: canvasSize).fill()

    let paragraph = NSMutableParagraphStyle()
    paragraph.alignment = .center
    paragraph.lineBreakMode = .byTruncatingMiddle
    let textAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.monospacedSystemFont(ofSize: 9, weight: .regular),
        .foregroundColor: NSColor(calibratedWhite: 0.12, alpha: 1),
        .paragraphStyle: paragraph
    ]
    let metaAttrs: [NSAttributedString.Key: Any] = [
        .font: NSFont.systemFont(ofSize: 8),
        .foregroundColor: NSColor(calibratedWhite: 0.36, alpha: 1),
        .paragraphStyle: paragraph
    ]

    for (index, asset) in assets.enumerated() {
        let column = index % columns
        let row = rows - 1 - (index / columns)
        let x = column * cellWidth
        let y = row * cellHeight
        let cellRect = NSRect(x: x, y: y, width: cellWidth, height: cellHeight)
        NSColor.white.setFill()
        NSBezierPath(rect: cellRect.insetBy(dx: 4, dy: 4)).fill()
        NSColor(calibratedWhite: 0.86, alpha: 1).setStroke()
        NSBezierPath(rect: cellRect.insetBy(dx: 4, dy: 4)).stroke()

        if let image = NSImage(contentsOf: asset.url) {
            let scale = min(Double(cellWidth - 18) / image.size.width, Double(imageHeight - 10) / image.size.height)
            let drawWidth = image.size.width * scale
            let drawHeight = image.size.height * scale
            let drawRect = NSRect(
                x: CGFloat(x) + (CGFloat(cellWidth) - drawWidth) / 2,
                y: CGFloat(y) + 42 + (CGFloat(imageHeight) - drawHeight) / 2,
                width: drawWidth,
                height: drawHeight
            )
            image.draw(in: drawRect)
        }

        let nameRect = NSRect(x: x + 8, y: y + 21, width: cellWidth - 16, height: 18)
        NSString(string: asset.name).draw(in: nameRect, withAttributes: textAttrs)
        let meta = "\(asset.stats.width)x\(asset.stats.height) bg \(asset.stats.backgroundHex)"
        NSString(string: meta).draw(in: NSRect(x: x + 8, y: y + 7, width: cellWidth - 16, height: 12), withAttributes: metaAttrs)
    }

    canvas.unlockFocus()

    guard let tiff = canvas.tiffRepresentation,
          let bitmap = NSBitmapImageRep(data: tiff),
          let png = bitmap.representation(using: .png, properties: [:]) else {
        throw NSError(domain: "ExerciseImageAudit", code: 4, userInfo: [NSLocalizedDescriptionKey: "Cannot render contact sheet"])
    }
    try png.write(to: outURL)
}

func markdownReport(assets: [Asset], refs: Set<String>, repoRoot: URL, resDir: URL, contactSheet: URL) -> String {
    let resourceNames = Set(assets.map(\.name))
    let imageRefs = refs.filter { $0.hasPrefix("exercise_") }
    let missingRefs = imageRefs.subtracting(resourceNames).sorted()
    let unreferenced = assets.filter { !$0.referenced }.map(\.name).sorted()
    let stepsByExercise = Dictionary(grouping: assets.filter { $0.kind == "step" }, by: \.exerciseId)
    let thumbnails = Set(assets.filter { $0.kind == "thumbnail" }.map(\.exerciseId))
    let missingThumbnails = stepsByExercise.keys.filter { !thumbnails.contains($0) }.sorted()
    let stepGaps = stepsByExercise.compactMap { exerciseId, steps -> String? in
        let numbers = Set(steps.compactMap(\.step))
        guard let maxStep = numbers.max(), maxStep > 0 else { return nil }
        let missing = (1...maxStep).filter { !numbers.contains($0) }
        return missing.isEmpty ? nil : "\(exerciseId): missing step \(missing.map(String.init).joined(separator: ", "))"
    }.sorted()

    let sizeGroups = Dictionary(grouping: assets) { "\($0.stats.width)x\($0.stats.height)" }
        .map { "\($0.key): \($0.value.count)" }
        .sorted()
        .joined(separator: ", ")

    var lines: [String] = []
    lines.append("# Exercise Image QA Report")
    lines.append("")
    lines.append("- Generated: \(ISO8601DateFormatter().string(from: Date()))")
    lines.append("- Resource directory: `\(relativePath(resDir, from: repoRoot))`")
    lines.append("- Contact sheet: `\(relativePath(contactSheet, from: repoRoot))`")
    lines.append("- Assets audited: \(assets.count) total, \(assets.filter { $0.kind == "step" }.count) steps, \(assets.filter { $0.kind == "thumbnail" }.count) thumbnails")
    lines.append("- Size distribution: \(sizeGroups)")
    lines.append("- Missing drawable references: \(missingRefs.count)")
    lines.append("- Unreferenced exercise assets: \(unreferenced.count)")
    lines.append("- Missing thumbnails for step sets: \(missingThumbnails.count)")
    lines.append("- Step numbering gaps: \(stepGaps.count)")
    lines.append("")
    lines.append("## Missing Referenced Files")
    lines.append("")
    lines.append(missingRefs.isEmpty ? "None." : missingRefs.map { "- `\($0)`" }.joined(separator: "\n"))
    lines.append("")
    lines.append("## Unreferenced Exercise Assets")
    lines.append("")
    lines.append(unreferenced.isEmpty ? "None." : unreferenced.map { "- `\($0)`" }.joined(separator: "\n"))
    lines.append("")
    lines.append("## Missing Thumbnails")
    lines.append("")
    lines.append(missingThumbnails.isEmpty ? "None." : missingThumbnails.map { "- `exercise_thumbnail_\($0)`" }.joined(separator: "\n"))
    lines.append("")
    lines.append("## Step Numbering Gaps")
    lines.append("")
    lines.append(stepGaps.isEmpty ? "None." : stepGaps.map { "- \($0)" }.joined(separator: "\n"))
    lines.append("")
    lines.append("## Asset Inventory")
    lines.append("")
    lines.append("| Asset | Kind | Exercise | Step | Size | Avg | Bg | Referenced |")
    lines.append("|---|---|---|---:|---:|---|---|---|")
    for asset in assets.sorted(by: { $0.name < $1.name }) {
        lines.append("| `\(asset.name)` | \(asset.kind) | `\(asset.exerciseId)` | \(asset.step.map(String.init) ?? "") | \(asset.stats.width)x\(asset.stats.height) | \(asset.stats.averageHex) | \(asset.stats.backgroundHex) | \(asset.referenced ? "yes" : "no") |")
    }
    lines.append("")
    return lines.joined(separator: "\n")
}

do {
    let config = parseArgs()
    let repoRoot = config.repoRoot.standardizedFileURL
    let resDir = (config.resDir ?? repoRoot.appendingPathComponent("feature/training/impl/src/main/res/drawable-nodpi")).standardizedFileURL
    let outDir = (config.outDir ?? repoRoot.appendingPathComponent("tmp/exercise-image-qa")).standardizedFileURL
    try FileManager.default.createDirectory(at: outDir, withIntermediateDirectories: true)

    let refs = referencedDrawables(in: [
        repoRoot.appendingPathComponent("app"),
        repoRoot.appendingPathComponent("core"),
        repoRoot.appendingPathComponent("feature")
    ])
    let assets = try discoverAssets(resDir: resDir, refs: refs)
    let contactSheet = outDir.appendingPathComponent("exercise-image-contact-sheet.png")
    let report = outDir.appendingPathComponent("exercise-image-qa-report.md")

    try writeContactSheet(assets: assets.sorted(by: { $0.name < $1.name }), to: contactSheet)
    try markdownReport(assets: assets, refs: refs, repoRoot: repoRoot, resDir: resDir, contactSheet: contactSheet)
        .write(to: report, atomically: true, encoding: .utf8)

    print("Audited \(assets.count) exercise images")
    print("Report: \(relativePath(report, from: repoRoot))")
    print("Contact sheet: \(relativePath(contactSheet, from: repoRoot))")
} catch {
    fputs("error: \(error.localizedDescription)\n", stderr)
    exit(1)
}
