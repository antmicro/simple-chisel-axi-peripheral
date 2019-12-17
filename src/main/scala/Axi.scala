/*
MIT License

Copyright (c) 2019 Antmicro

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package Axi
import chisel3._
import chisel3.util._

class AxiPeriph(val busWidth:Int, val registersCount:Int) extends Module {
    val io = IO(new Bundle{
      val axi_s0 = Flipped(new AXI4Lite(log2Ceil(registersCount*4), busWidth))
      val leds = Output(UInt(2.W))
      val irqOut = Output(Bool())
      })

    val regs = RegInit(VecInit(Seq.fill(registersCount)(0.U(busWidth.W))))
    val slaveInterface = Module(new AXI4LiteCSR(log2Ceil(registersCount*4), busWidth))

    io.irqOut := regs(0)(4)

    /* interface connections */
    slaveInterface.io.ctl <> io.axi_s0
    io.leds := regs(0)(1, 0)

    /* handle registers */
    when(slaveInterface.io.bus.write) {
      regs(slaveInterface.io.bus.addr) := slaveInterface.io.bus.dataOut
    }

    slaveInterface.io.bus.dataIn := regs(slaveInterface.io.bus.addr)
}

object AxiPeriph extends App {

  chisel3.Driver.execute(args, () => new AxiPeriph(32, 8))
}

