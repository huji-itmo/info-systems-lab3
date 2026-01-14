import json
import random
from faker import Faker

fake = Faker()

# Constants from OpenAPI spec
WEAPON_TYPES = ["BOLTGUN", "HEAVY_BOLTGUN", "FLAMER", "HEAVY_FLAMER", "MULTI_MELTA"]
CATEGORIES = ["AGGRESSOR", "INCEPTOR", "TACTICAL", "CHAPLAIN", "APOTHECARY"]

def generate_spacemarine_with_ids(index: int):
    name = f"Brother_{fake.lexify(text='????').upper()}{index:03d}"
    return {
        "name": name,
        "coordinatesId": random.randint(1, 100),  # Random existing coordinates ID
        "chapterId": random.randint(1, 100),     # Random existing chapter ID
        "health": fake.random_int(min=1, max=200),
        "loyal": random.choice([True, False, None]),  # nullable field
        "category": random.choice(CATEGORIES + [None]),
        "weaponType": random.choice(WEAPON_TYPES)
    }

# Generate 1000 Space Marines with ID references
data = [generate_spacemarine_with_ids(i) for i in range(1, 1001)]

# Save as JSON array (valid for import)
with open("spacemarines_with_ids_1000.json", "w") as f:
    json.dump(data, f, indent=2)

print("✅ Generated spacemarines_with_ids_1000.json")
