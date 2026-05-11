import { useEffect, useRef } from "react";
import JsBarcode from "jsbarcode";

export default function Barcode({ value, width = 1.5, height = 40, fontSize = 12, displayValue = true, margin = 2 }) {
  const svgRef = useRef(null);

  useEffect(() => {
    if (!value || !svgRef.current) return;
    try {
      JsBarcode(svgRef.current, value, {
        format: "CODE128",
        width,
        height,
        fontSize,
        displayValue,
        margin,
        lineColor: "#1a1a1a",
        background: "#ffffff",
      });
    } catch (e) {
      svgRef.current.innerHTML = `<text fill="red" font-size="10">${value}</text>`;
    }
  }, [value, width, height, fontSize, displayValue, margin]);

  if (!value) return <span className="text-muted">-</span>;
  return <svg ref={svgRef} />;
}
