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
    uart_print("Hello from Rust! ");
    uart_print("Fibonacci(5): ");
    let n : u32 = 5;
    for i in 1..n + 1 {
        uart_write(b'0' + (fibonacci(i) as u8));
    }
    loop {};
}

fn fibonacci(n: u32) -> u32 {
    if n <= 1 { return n; }
    let mut current : u32 = 0;
    let mut previous_a : u32 = 1;
    let mut previous_b : u32 = 0;

    for _ in 2..n+1  {
        current = previous_a + previous_b;
        previous_b = previous_a;
        previous_a = current;
    }
    current
}
