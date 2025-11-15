# Basic 3D Projections
3D Rendering from scratch using **Jetpack Compose Canvas** and **linear algebra**

## Introduction
How far can we push Jetpack Compose and Canvas? With a bit of ingenuity and math, we can jump from 2D space into the third dimension, using just pure `linear algebra` and `Canvas`.

This project explores the fundamentals of computer graphics by implementing 3D projections, transformations and rendering entirely from scratch.

## Demo
### Built-in Geometric Shapes

https://github.com/user-attachments/assets/83675354-ca9c-42e3-b2e2-3a0b69dbe5e7

### OBJ File import

https://github.com/user-attachments/assets/86349a56-f9ee-4b3e-8ed3-3985efa0219e

## Known limitations
This project perform all 3D calculations and rendering on the UI thread during the draw phase.
While this approach works for simple shapes, complex shapes can freeze the UI since they require thousands of operations per frame (rotation, scaling, projection and drawing).
