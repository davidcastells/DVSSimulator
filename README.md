# DVSSimulator

## What DVS means?
"Dynamic Vision Systems" or "Event-Based Cameras" are image sensors that work on a different principle than the standard frame-based cameras that we all carry on our mobile-phones. They emit events as they happen in the scene.

Watch the following video to understand how they work.<br>
https://www.youtube.com/watch?v=kPCZESVfHoQ

## Why do we want to simulate event-based cameras?
This type of Hardware is still not so widespread, although you can buy them from different companies such as 

https://www.prophesee.ai
https://www.inivation.com
http://www.celepixel.com/

Instead of frames, event-based cameras generate event streams with time, location and event information. We want to synthetically generate this kind of streams to test event-based vision algorithms.

Our approach is very simple. You can find a more complex simulator implementation in
https://github.com/uzh-rpg/rpg_esim

## Research Paper

This is part of research publication. We will be happy if you cite our paper

>David Castells-Rufas, Jordi Carrabina. "OpenCL-based FPGA accelerator for disparity map generation with stereoscopic event cameras". In Proceedings of the International Workshop on High Performance Energy-Efficient Embedded Systems (HIP3ES) 2019. Valencia, Spain.


 
