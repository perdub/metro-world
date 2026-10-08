#!/usr/bin/env python3
"""Original numeric instrument textures; the actual readings are selected by the server."""
from pathlib import Path
import json
from PIL import Image,ImageDraw,ImageFont
root=Path(__file__).resolve().parents[1]/'src/main/resources/assets/btr_infrastructure'
font=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',18)
for instrument,maximum in [('speed',44),('pressure',8),('traction',5),('brake',7)]:
 for value in range(maximum+1):
  name=f'train_{instrument}_{value}'
  image=Image.new('RGBA',(32,32),(12,20,25,255));draw=ImageDraw.Draw(image)
  draw.rounded_rectangle((0,0,31,31),radius=4,outline=(76,142,145,255),width=2)
  label=str(value);box=draw.textbbox((0,0),label,font=font);draw.text(((32-(box[2]-box[0]))/2,(32-(box[3]-box[1]))/2-box[1]),label,font=font,fill=(185,246,216,255))
  image.save(root/'textures/item'/f'{name}.png')
  (root/'models/item'/f'{name}.json').write_text(json.dumps({'parent':'minecraft:item/generated','textures':{'layer0':f'btr_infrastructure:item/{name}'}},indent=2)+'\n')
