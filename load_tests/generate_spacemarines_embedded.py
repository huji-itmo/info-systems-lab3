import json
import random
from faker import Faker

fake = Faker()

# Constants from your OpenAPI spec
WEAPON_TYPES = ["BOLTGUN", "HEAVY_BOLTGUN", "FLAMER", "HEAVY_FLAMER", "MULTI_MELTA"]
CATEGORIES = ["AGGRESSOR", "INCEPTOR", "TACTICAL", "CHAPLAIN", "APOTHECARY"]
CHAPTER_NAMES = [
    "Ultramarines", "Blood Angels", "Dark Angels", "Space Wolves",
    "Imperial Fists", "White Scars", "Salamanders", "Raven Guard",
    "Iron Hands", "Alpha Legion", "Night Lords", "Word Bearers"
]

def generate_embedded_spacemarine(index: int):
    name = f"Brother_{fake.lexify(text='????').upper()}{index:03d}"
    return {
        "name": name,
        "coordinates": {
            "x": fake.random_int(min=-1000, max=1000),
            "y": round(fake.pyfloat(min_value=-343, max_value=343), 2)  # y <= 343
        },
        "chapter": {
            "name": random.choice(CHAPTER_NAMES) + f"_{index % 4}",
            "marinesCount": fake.random_int(min=10, max=1000)
        },
        "health": fake.random_int(min=1, max=200),
        "loyal": random.choice([True, False, None]),  # nullable
        "category": random.choice(CATEGORIES + [None]),
        "weaponType": random.choice(WEAPON_TYPES)
    }

# Generate 100 embedded marines
data = [generate_embedded_spacemarine(i) for i in range(1, 1001)]

# Save as JSON array (valid for import)
with open("spacemarines_embedded_1000.json", "w") as f:
    json.dump(data, f, indent=2)

print("✅ Generated spacemarines_embedded_1000.json")
