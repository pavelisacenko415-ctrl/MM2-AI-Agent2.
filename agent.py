from pathlib import Path
from PIL import Image
import sys

def analyze_screenshot(path):
    file = Path(path)

    if not file.is_file():
        print(f"Изображение не найдено: {file}")
        return

    try:
        image = Image.open(file).convert("RGB")
    except Exception as error:
        print(f"Не удалось открыть изображение: {error}")
        return

    width, height = image.size
    print("=== MM2 AI: анализ экрана ===")
    print(f"Размер изображения: {width} x {height}")

    sample = image.resize((1, 1)).getpixel((0, 0))
    print(f"Средний цвет экрана: RGB{sample}")
    print("Скриншот получен и обработан.")
    print("Следующий этап: поиск игровых объектов.")

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print("Использование: python agent.py путь_к_скриншоту")
    else:
        analyze_screenshot(sys.argv[1])
