import os
import json
from pathlib import Path
from openai import OpenAI
from dotenv import load_dotenv

load_dotenv()

client = OpenAI(
    base_url="https://openrouter.ai/api/v1",
    api_key=os.getenv("OPENROUTER_API_KEY"),
)

# =========================
# Model
# =========================
model = "x-ai/grok-4.6"

# =========================
# Path
# =========================
BASE_DIR = Path(__file__).resolve().parent.parent

prompt_path = BASE_DIR / "applications" / "prompt.txt"
output_dir = BASE_DIR / "applications" / "grok"

# =========================
# Read prompt
# =========================
prompt = prompt_path.read_text(encoding="utf-8")

prompt += """

IMPORTANT OUTPUT FORMAT:

Return ONLY valid JSON.
Do not use Markdown code fences.
Do not provide explanations outside the JSON.

The JSON must have this structure:

{
  "files": [
    {
      "path": "pom.xml",
      "content": "..."
    },
    {
      "path": "src/main/java/example/Application.java",
      "content": "..."
    }
  ]
}

Each file must contain its complete content.
Use relative file paths only.
"""

# =========================
# Call OpenRouter
# =========================
response = client.chat.completions.create(
    model=model,
    messages=[
        {
            "role": "user",
            "content": prompt
        }
    ],
)

result = response.choices[0].message.content

# =========================
# Parse JSON
# =========================
try:
    project = json.loads(result)
except json.JSONDecodeError:
    print("ERROR: Model did not return valid JSON.")
    print(result)
    raise SystemExit(1)

# =========================
# Create project directory
# =========================
output_dir.mkdir(parents=True, exist_ok=True)

# =========================
# Create files
# =========================
for file in project["files"]:

    relative_path = Path(file["path"])

    # Prevent absolute paths
    if relative_path.is_absolute():
        raise ValueError(f"Invalid absolute path: {relative_path}")

    file_path = output_dir / relative_path

    file_path.parent.mkdir(parents=True, exist_ok=True)

    file_path.write_text(
        file["content"],
        encoding="utf-8"
    )

    print(f"Created: {file_path}")

print()
print("================================")
print("Project generation completed.")
print(f"Project location: {output_dir}")
print("================================")