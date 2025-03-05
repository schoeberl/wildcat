#![no_std]
#![no_main]

use core::panic::PanicInfo;

const UART_ADDR: *mut u32 = 0xf000_0004 as *mut u32;

#[panic_handler]
fn panic(_info: &PanicInfo) -> ! {
    loop {};
}

fn uart_write(c: u8) {
    unsafe {
        UART_ADDR.write_volatile(c as u32);
    }
}

fn uart_print(s: &str) {
    for byte in s.bytes() {
        uart_write(byte);
    }
}

#[unsafe(no_mangle)]
pub extern "C" fn _start() -> ! {
    uart_print("Hello from Rust!");
    loop {};
}
