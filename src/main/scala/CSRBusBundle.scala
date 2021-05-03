/*
 * Copyright (c) 2019-2021 Antmicro <www.antmicro.com>
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package Axi

import chisel3._
import chisel3.util.log2Ceil

class CSRBusBundle(val addrWidth:Int, val busWidth:Int) extends Bundle {
  val addr = Output(UInt(addrWidth.W))
  val dataOut = Output(UInt(busWidth.W))
  val dataIn = Input(UInt(busWidth.W))
  val write = Output(Bool())
  val read = Output(Bool())
}
