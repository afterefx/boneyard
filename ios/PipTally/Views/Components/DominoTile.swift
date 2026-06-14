import SwiftUI

struct DominoTile: View {
    let topValue: Int
    let bottomValue: Int
    var tileWidth: CGFloat = 48
    var isVertical: Bool = true
    var backgroundColor: Color? = nil
    var pipColor: Color? = nil
    var dividerColor: Color? = nil
    
    private let pipPositions: [[(CGFloat, CGFloat)]] = [
        [], // 0
        [(0.5, 0.5)], // 1
        [(0.25, 0.3), (0.75, 0.7)], // 2
        [(0.25, 0.25), (0.5, 0.5), (0.75, 0.75)], // 3
        [(0.25, 0.25), (0.75, 0.25), (0.25, 0.75), (0.75, 0.75)], // 4
        [(0.25, 0.25), (0.75, 0.25), (0.5, 0.5), (0.25, 0.75), (0.75, 0.75)], // 5
        [
            (0.25, 0.2), (0.75, 0.2),
            (0.25, 0.5), (0.75, 0.5),
            (0.25, 0.8), (0.75, 0.8)
        ] // 6
    ]
    
    var body: some View {
        let tileHeight = tileWidth * 2
        let size = isVertical ? CGSize(width: tileWidth, height: tileHeight) : CGSize(width: tileHeight, height: tileWidth)
        let tileBackground = backgroundColor ?? (Color(hex: "#1A1A1A") ?? .black)
        let tilePipColor = pipColor ?? (Color(hex: "#F5F0E8") ?? .white)
        let tileDividerColor = dividerColor ?? (Color(hex: "#3A3A3A") ?? .gray)
        
        Canvas { context, size in
            let w = size.width
            let h = size.height
            let cornerRadius = min(w, h) * 0.12
            let faceShortSide = isVertical ? w : h
            let pipRadius = faceShortSide * 0.07
            
            // Draw background Rounded Rectangle
            let rect = CGRect(origin: .zero, size: size)
            let path = Path(roundedRect: rect, cornerRadius: cornerRadius)
            context.fill(path, with: .color(tileBackground))
            
            // Draw Divider Line
            var dividerPath = Path()
            if isVertical {
                dividerPath.move(to: CGPoint(x: w * 0.12, y: h * 0.5))
                dividerPath.addLine(to: CGPoint(x: w * 0.88, y: h * 0.5))
                context.stroke(dividerPath, with: .color(tileDividerColor), style: StrokeStyle(lineWidth: w * 0.03))
            } else {
                dividerPath.move(to: CGPoint(x: w * 0.5, y: h * 0.12))
                dividerPath.addLine(to: CGPoint(x: w * 0.5, y: h * 0.88))
                context.stroke(dividerPath, with: .color(tileDividerColor), style: StrokeStyle(lineWidth: h * 0.03))
            }
            
            // Draw Pips helper
            func drawPips(value: Int, xOffset: CGFloat, yOffset: CGFloat, faceW: CGFloat, faceH: CGFloat) {
                let clampedValue = max(0, min(6, value))
                let pips = pipPositions[clampedValue]
                let padding = faceW * 0.1
                
                for (xFrac, yFrac) in pips {
                    let px = xOffset + padding + xFrac * (faceW - 2 * padding)
                    let py = yOffset + padding + yFrac * (faceH - 2 * padding)
                    
                    let pipRect = CGRect(x: px - pipRadius, y: py - pipRadius, width: pipRadius * 2, height: pipRadius * 2)
                    let pipPath = Path(ellipseIn: pipRect)
                    context.fill(pipPath, with: .color(tilePipColor))
                }
            }
            
            if isVertical {
                drawPips(value: topValue, xOffset: 0, yOffset: 0, faceW: w, faceH: h * 0.5)
                drawPips(value: bottomValue, xOffset: 0, yOffset: h * 0.5, faceW: w, faceH: h * 0.5)
            } else {
                drawPips(value: topValue, xOffset: 0, yOffset: 0, faceW: w * 0.5, faceH: h)
                drawPips(value: bottomValue, xOffset: w * 0.5, yOffset: 0, faceW: w * 0.5, faceH: h)
            }
        }
        .frame(width: size.width, height: size.height)
    }
}
