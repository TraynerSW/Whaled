import sys
from PIL import Image

def resize_img(input_path, output_path, size):
    img = Image.open(input_path)
    img = img.resize((size, size), Image.Resampling.LANCZOS)
    img.save(output_path, format="PNG")

input_file = sys.argv[1]
resize_img(input_file, "app/src/main/res/mipmap-mdpi/ic_launcher.png", 48)
resize_img(input_file, "app/src/main/res/mipmap-hdpi/ic_launcher.png", 72)
resize_img(input_file, "app/src/main/res/mipmap-xhdpi/ic_launcher.png", 96)
resize_img(input_file, "app/src/main/res/mipmap-xxhdpi/ic_launcher.png", 144)
resize_img(input_file, "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png", 192)

resize_img(input_file, "app/src/main/res/mipmap-mdpi/ic_launcher_round.png", 48)
resize_img(input_file, "app/src/main/res/mipmap-hdpi/ic_launcher_round.png", 72)
resize_img(input_file, "app/src/main/res/mipmap-xhdpi/ic_launcher_round.png", 96)
resize_img(input_file, "app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png", 144)
resize_img(input_file, "app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png", 192)
