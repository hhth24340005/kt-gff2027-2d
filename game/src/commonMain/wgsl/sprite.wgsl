struct Placement {
  canvas_size: vec2f,
  // The bottom-left corner in pixels, with y pointing up
  position: vec2f,
  size: vec2f,
}

@group(0) @binding(0) var sprite: texture_2d<f32>;
@group(0) @binding(1) var sprite_sampler: sampler;
@group(0) @binding(2) var<uniform> placement: Placement;

struct VertexOutput {
  @builtin(position) position: vec4f,
  @location(0) uv: vec2f,
}

@vertex
fn vs_main(@builtin(vertex_index) i: u32) -> VertexOutput {
  let corners = array(
    vec2f(0.0, 0.0), vec2f(1.0, 0.0), vec2f(0.0, 1.0),
    vec2f(0.0, 1.0), vec2f(1.0, 0.0), vec2f(1.0, 1.0),
  );
  let corner = corners[i];
  let pixel = placement.position + corner * placement.size;
  let position = normalize_to_clip(pixel, placement.canvas_size);
  // uv points y down, as images list their rows top first
  let uv = vec2f(corner.x, 1.0 - corner.y);
  return VertexOutput(vec4f(position, 0.0, 1.0), uv);
}

// Normalizes pixel from [0, canvas_size] to [-1, 1]
fn normalize_to_clip(pixel: vec2f, canvas_size: vec2f) -> vec2f {
  return pixel / canvas_size * 2.0 - 1.0;
}

@fragment
fn fs_main(@location(0) uv: vec2f) -> @location(0) vec4f {
  let color = textureSample(sprite, sprite_sampler, uv);
  if color.a < 0.5 {
    discard;
  }
  return color;
}
