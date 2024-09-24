module register_file(
    input clock,
    input [4:0] rs1,
    input [4:0] rs2,
    input [4:0] wr_addr,
    input [31:0] wr_data,
    input wr_ena,
    output reg [31:0] read_val1,
    output reg [31:0] read_val2
);

    // Declare the register file
    reg [31:0] reg_file [31:0];

    // Read addresses
    reg [4:0] read_addr1;
    reg [4:0] read_addr2;
    // Read data
    reg [31:0] read_data1;
    reg [31:0] read_data2;

    // Write
    reg [4:0] write_addr;
    reg [31:0] write_data;
    reg write_ena;

//`define INPUT_RD_REG
//`define INPUT_WR_REG
//`define OUPUT_REG
//`define FF_MEM
//`define LATCH_MEM
`define LATCH_HOT


`ifdef INPUT_RD_REG
    always @(posedge clock) begin
        read_addr1 <= rs1;
        read_addr2 <= rs2;
    end
`else
    always @(*) begin
        read_addr1 = rs1;
        read_addr2 = rs2;
    end
`endif

`ifdef INPUT_WR_REG
    always @(posedge clock) begin
        write_addr <= wr_addr;
        write_data <= wr_data;
        write_ena <= wr_ena;
    end
`else
    always @(*) begin
        write_addr = wr_addr;
        write_data = wr_data;
        write_ena = wr_ena;
    end
`endif

`ifdef FF_MEM
    // Read ports
    always @(*) begin
        read_data1 = reg_file[read_addr1];
        read_data2 = reg_file[read_addr2];
    end

    // Write port
    always @(posedge clock) begin
        if (write_ena) begin
            reg_file[write_addr] <= write_data;
        end
    end
`endif

// Does not synthsis with OpenLane
// Probably good so (as discussed with Tommy)
`ifdef LATCH_MEM
    always @(*) begin
        read_data1 = reg_file[read_addr1];
        read_data2 = reg_file[read_addr2];
    end

    // OpenLane doe not like this
    // always @(*) begin
    always_latch begin
        if (write_ena & clock) begin
            reg_file[write_addr] = write_data;
        end
    end
`endif

`ifdef LATCH_HOT
    always @(*) begin
        read_data1 = reg_file[read_addr1];
        read_data2 = reg_file[read_addr2];
    end

genvar i;
generate
    for (i = 0; i < 1; i = i + 1) begin : gen_block
        always_latch begin
            if (write_addr == i & write_ena & clock) begin
                reg_file[i] = write_data;
            end
        end
    end
endgenerate

`endif

`ifdef OUTPUT_REG
    always @(posedge clock) begin
        read_val1 <= read_data1;
        read_val2 <= read_data2;
    end
`else
    assign read_val1 = read_data1;
    assign read_val2 = read_data2;
`endif

endmodule