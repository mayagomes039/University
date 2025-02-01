export function validateParams(toolName, params) {
    switch (toolName) {
      case "border":
        return (
          params.border_width > 0 &&
          // have to check or do parsing of the color
          params.r >= 0 && params.r <= 255 &&
          params.g >= 0 && params.g <= 255 &&
          params.b >= 0 && params.b <= 255
        );
  
      case "brightness":
        return params.brightness_factor >= 0 && params.brightness_factor <= 2;
  
      case "contrast":
        return params.contrast_factor >= 0 && params.contrast_factor <= 2;
  
      case "crop":
        return (
          params.left >= 0 &&
          params.upper >= 0 &&
          params.right > params.left &&
          params.lower > params.upper
        );
  
      case "rotate":
        return params.angle >= 0 && params.angle < 360;
  
      case "scale":
        return params.new_width > 0 && params.new_height > 0;
  
      case "watermark":
        return (
          //typeof params.watermarkImageURI === "string" &&
          params.scale_factor > 0 &&
          params.opacity >= 0 && params.opacity <= 1 &&
          params.positionX > 0 &&
          params.positionY > 0
        );
  
      default:
        // se nao tiver especificações
        return true;
    }
  }
  