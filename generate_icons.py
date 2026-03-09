
import os
import sys
from PIL import Image, ImageDraw, ImageOps, ImageFont

def create_base_image(path):
    size = 512
    # Dark background
    img = Image.new('RGBA', (size, size), color=(23, 23, 23, 255))
    draw = ImageDraw.Draw(img)
    
    # White shield-like shape
    # Simple shield shape: top flat, sides curved to bottom point
    # Coordinates for a simple shield
    margin = 100
    p1 = (margin, margin) # Top left
    p2 = (size - margin, margin) # Top right
    p3 = (size - margin, size - margin * 1.5) # Bottom right curve start
    p4 = (size // 2, size - margin) # Bottom point
    p5 = (margin, size - margin * 1.5) # Bottom left curve start

    draw.polygon([p1, p2, p4, p5], fill=(255, 255, 255, 255))
    
    # Add a "P" for Private
    try:
        # Try to load a default font, otherwise skip text
        font = ImageFont.truetype("arial.ttf", 200)
        # simplistic centering - might be off but acceptable for placeholder
        draw.text((size//2 - 60, size//2 - 120), "P", font=font, fill=(23, 23, 23, 255))
    except:
        pass
        
    img.save(path)
    print(f"Created base placeholder image at {path}")
    return path

def create_icons(base_image_path, res_dir):
    # Ensure base image exists, create if not
    if not os.path.exists(base_image_path):
        create_base_image(base_image_path)

    img = Image.open(base_image_path).convert("RGBA")

    # Densities and sizes
    densities = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }

    for folder, size in densities.items():
        out_folder = os.path.join(res_dir, (folder))
        os.makedirs(out_folder, exist_ok=True)
        
        # Resize for ic_launcher.png (square/full)
        icon = img.resize((size, size), Image.Resampling.LANCZOS)
        icon.save(os.path.join(out_folder, 'ic_launcher.png'))

        # Create round icon
        mask = Image.new('L', (size, size), 0)
        draw = ImageDraw.Draw(mask)
        draw.ellipse((0, 0, size, size), fill=255)
        
        # Standardize round icon - crop to circle
        output = ImageOps.fit(img, (size, size), centering=(0.5, 0.5))
        output.putalpha(mask)
        output.save(os.path.join(out_folder, 'ic_launcher_round.png'))
        
    print("All icons generated successfully.")

if __name__ == "__main__":
    if len(sys.argv) > 2:
        create_icons(sys.argv[1], sys.argv[2])
    else:
        # Default behavior for manual run
        base = "base_icon.png"
        res = "android/app/src/main/res" # Relative path assumption if run from root
        create_icons(base, res)
