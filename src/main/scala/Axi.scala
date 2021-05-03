/*
 * Copyright (c) 2019-2021 Antmicro <www.antmicro.com>
 *
 * SPDX-License-Identifier: Apache-2.0
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

