all: verilog
verilog:
	sbt "runMain Axi.AxiPeriph"
clean-verilog:
	rm -rf *.v

clean: clean-verilog
