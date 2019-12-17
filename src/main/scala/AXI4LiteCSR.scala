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

class AXI4LiteCSR(val addrWidth:Int, val busWidth:Int) extends Module{
  val io = IO(new Bundle{
    val ctl = Flipped(new AXI4Lite(addrWidth, busWidth))
    val bus = new CSRBusBundle(addrWidth, busWidth)
  })

  val sIdle :: sReadAddr :: sReadData :: sWriteAddr :: sWriteData :: sWriteResp :: Nil = Enum(6)
  val state = RegInit(sIdle)

  val awready = RegInit(false.B)
  val wready = RegInit(false.B)
  val bvalid = RegInit(false.B)
  val bresp = WireInit(0.U(AXI4Lite.respWidth.W))

  val arready = RegInit(false.B)
  val rvalid = RegInit(false.B)
  val rresp = WireInit(0.U(AXI4Lite.respWidth.W))

  val addr = RegInit(0.U(addrWidth.W))

  val read = RegInit(false.B)
  val write = RegInit(false.B)
  val dataOut = RegInit(0.U(busWidth.W))

  val transaction_id = RegInit(0.U(AXI4Lite.idWidth.W))

  io.ctl.r.rdata := io.bus.dataIn
  io.ctl.r.rid := transaction_id
  io.bus.dataOut := dataOut

  io.ctl.aw.awready := awready
  io.ctl.w.wready := wready
  io.ctl.b.bvalid := bvalid
  io.ctl.b.bresp := bresp
  io.ctl.b.bid := transaction_id

  io.ctl.ar.arready := arready
  io.ctl.r.rvalid := rvalid
  io.ctl.r.rresp := rresp

  io.bus.read := read
  io.bus.write := write
  io.bus.addr := addr

  switch(state){
    is(sIdle){
      rvalid := false.B
      bvalid := false.B
      read := false.B
      write := false.B
      transaction_id := 0.U
      when(io.ctl.aw.awvalid){
        state := sWriteAddr
        transaction_id := io.ctl.aw.awid
      }.elsewhen(io.ctl.ar.arvalid){
        state := sReadAddr
        transaction_id := io.ctl.ar.arid
      }
    }
    is(sReadAddr){
      arready := true.B
      when(io.ctl.ar.arvalid && arready){
        state := sReadData
        addr := io.ctl.ar.araddr(addrWidth - 1, 2)
        read := true.B
        arready := false.B
      }
    }
    is(sReadData){
      rvalid := true.B
      when(io.ctl.r.rready && rvalid){
        state := sIdle
        rvalid := false.B
      }
    }
    is(sWriteAddr){
      awready := true.B
      when(io.ctl.aw.awvalid && awready){
        addr := io.ctl.aw.awaddr(addrWidth - 1, 2)
        state := sWriteData
        awready := false.B
      }
    }
    is(sWriteData){
      wready := true.B
      when(io.ctl.w.wvalid && wready){
        state := sWriteResp
        dataOut := io.ctl.w.wdata
        write := true.B
        wready := false.B
      }
    }
    is(sWriteResp){
      wready := false.B
      bvalid := true.B
      when(io.ctl.b.bready && bvalid){
        state := sIdle
        bvalid := false.B
      }
    }
  }
}
