#version 300 es
precision highp float;

uniform float uScale;
uniform vec2 uCenter;

in vec2 coords;
in vec2 vScreen;

out vec4 color;

precision highp float;
#define ITERATIONS 200

float user_scale = 1.0;
float canvas_resolution = 800.0;
float center = 400.0;
int steps = 100;

vec2 cmul(vec2 a, vec2 b) {
    return vec2(a.x * b.x - a.y * b.y, a.x * b.y + a.y * b.x);
}

float magnitude(vec2 v) {
    return sqrt(v.x * v.x + v.y * v.y);
}

vec3 val_at(vec2 pos) {
    vec2 z = pos;
    for (int i = 0; i < ITERATIONS; i++) {
        z = cmul(z, z) - vec2(1.0, 0.0);
        if (magnitude(z) > 4.) {
            return vec3(sqrt(float(i + 1) - log(log(magnitude(z))) / log(2.)));
        }
    }
    return vec3(z, z.x + z.y) * 5.;
}

void main()
{
    // Normalized pixel coordinates (from 0 to 1)
    vec2 uv = coords;
    uv *= uScale;
    uv += uCenter;

    vec3 calculated = val_at(uv);
    //vec3 calculated = val_at((gl_FragCoord.xy / canvas_resolution - 0.5) * user_scale + center);

    color = vec4(sin(calculated * 0.5 + float(steps) * 0.01) / 2. + 0.5, 1.0);
}