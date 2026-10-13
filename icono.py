# Genera resources/icon.png y resources/splash.png (lo corre GitHub al compilar)
import math, os
from PIL import Image, ImageDraw, ImageFont
os.makedirs('resources', exist_ok=True)
S=1024; im=Image.new('RGB',(S,S),(47,75,99)); d=ImageDraw.Draw(im)
cx=cy=S/2; R1,R2=400,290
cols=[(242,193,78)]*3+[(223,229,234)]*3+[(242,193,78)]*2+[(223,229,234)]*2
for i,c in enumerate(cols):
    a0=-90+i*36+2; a1=a0+32
    pts=[(cx+R1*math.cos(math.radians(a0+(a1-a0)*t/20)),cy+R1*math.sin(math.radians(a0+(a1-a0)*t/20))) for t in range(21)]
    pts+=[(cx+R2*math.cos(math.radians(a1-(a1-a0)*t/20)),cy+R2*math.sin(math.radians(a1-(a1-a0)*t/20))) for t in range(21)]
    d.polygon(pts,fill=c)
try: f=ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf',300)
except Exception: f=ImageFont.load_default()
d.text((cx,cy),'$',font=f,fill=(240,180,58),anchor='mm')
im.save('resources/icon.png')
Image.new('RGB',(2732,2732),(47,75,99)).save('resources/splash.png')
print('iconos listos')
