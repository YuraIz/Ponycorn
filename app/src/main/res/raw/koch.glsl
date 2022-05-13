#version 300 es
precision highp float;

uniform float uScale;
uniform vec2 uCenter;

in vec2 coords;
in vec2 vScreen;

out vec4 color;

float koch(vec2 p)
{
    p = abs(fract(p)-0.5);
    for(int i=0;i<50;++i)
    {
        p = -vec2(-p.y + p.x*1.735, abs(p.x + p.y*1.735) - 0.58)*.865;
    }

    return p.x;
}

void main()
{
    vec2 p = coords;
    p*=0.5;
    p *= uScale;
    p += uCenter;
    p = clamp(p,-.55,.55);

    float rz = koch(p.yx*.9+vec2(0.5,0));

    vec3 col = vec3(1.0);

    if(rz > 0.5) {
        col = vec3(0.0);
    }

    color = vec4(col,1.0);
}