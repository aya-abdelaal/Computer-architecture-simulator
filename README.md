# Computer-architecture-simulator
Simulated a Von Neumann processor behavior and components using Java.

It has an 11 bit indexed memory, 33 registers, and pipelines instructions using 5 stages.

It parses and performs these instructions:

![image](https://github.com/aya-abdelaal/Computer-architecture-simulator/assets/67323302/87ebf2ce-70d7-432e-a01f-d17c5ccb2436)


Pipelining pattern is as follows:
– You fetch an instruction every 2 clock cycles starting from clock cycle 1.
– An instruction stays in the Decode (ID) stage for 2 clock cycles.
– An instruction stays in the Execute (EX) stage for 2 clock cycles.
– An instruction stays in the Memory (MEM) stage for 1 clock cycle.
– An instruction stays in the Write Back (WB) stage for 1 clock cycle.
– You can not have the Instruction Fetch (IF) and Memory (MEM) stages working in parallel. Only one of them is active at a given clock cycle.
