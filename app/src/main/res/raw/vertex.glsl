#version 300 es
uniform mat4 uMVPMatrix;
in vec2 vPosition;
out vec2 coords;
void main() {
    gl_Position = vec4(vPosition.xy, 0.0, 1.0) * uMVPMatrix;
    coords = vPosition.xy;
}