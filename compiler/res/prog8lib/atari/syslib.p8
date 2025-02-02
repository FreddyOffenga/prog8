; Prog8 definitions for the Atari800XL
; Including memory registers, I/O registers, Basic and Kernal subroutines.
;
; Written by Irmen de Jong (irmen@razorvine.net) - license: GNU GPL 3.0
;

atari {

        &uword  NMI_VEC         = $FFFA     ; 6502 nmi vector, determined by the kernal if banked in
        &uword  RESET_VEC       = $FFFC     ; 6502 reset vector, determined by the kernal if banked in
        &uword  IRQ_VEC         = $FFFE     ; 6502 interrupt vector, determined by the kernal if banked in

; ---- kernal routines ----
; TODO


asmsub  init_system()  {
    ; Initializes the machine to a sane starting state.
    ; Called automatically by the loader program logic.
    ; TODO
    %asm {{
        sei
        cld
        clc
        ; TODO reset screen mode etc etc
        clv
        cli
        rts
    }}
}

asmsub  init_system_phase2()  {
    %asm {{
        rts     ; no phase 2 steps on the Atari
    }}
}

}


sys {
    ; ------- lowlevel system routines --------

    const ubyte target = 8         ;  compilation target specifier.  64 = C64, 128 = C128,  16 = CommanderX16, 8 = atari800XL


    asmsub  reset_system()  {
        ; Soft-reset the system back to initial power-on Basic prompt.
        ; TODO
        %asm {{
            sei
            jmp  (atari.RESET_VEC)
        }}
    }

    sub wait(uword jiffies) {
        ; --- wait approximately the given number of jiffies (1/60th seconds)
        ;     TODO
    }

    asmsub waitvsync() clobbers(A) {
        ; --- busy wait till the next vsync has occurred (approximately), without depending on custom irq handling.
        ;     TODO
        %asm {{
            nop
            rts
        }}
    }

    asmsub memcopy(uword source @R0, uword target @R1, uword count @AY) clobbers(A,X,Y) {
        ; note: can't be inlined because is called from asm as well
        %asm {{
            ldx  cx16.r0
            stx  P8ZP_SCRATCH_W1        ; source in ZP
            ldx  cx16.r0+1
            stx  P8ZP_SCRATCH_W1+1
            ldx  cx16.r1
            stx  P8ZP_SCRATCH_W2        ; target in ZP
            ldx  cx16.r1+1
            stx  P8ZP_SCRATCH_W2+1
            cpy  #0
            bne  _longcopy

            ; copy <= 255 bytes
            tay
            bne  _copyshort
            rts     ; nothing to copy

_copyshort
            ; decrease source and target pointers so we can simply index by Y
            lda  P8ZP_SCRATCH_W1
            bne  +
            dec  P8ZP_SCRATCH_W1+1
+           dec  P8ZP_SCRATCH_W1
            lda  P8ZP_SCRATCH_W2
            bne  +
            dec  P8ZP_SCRATCH_W2+1
+           dec  P8ZP_SCRATCH_W2

-           lda  (P8ZP_SCRATCH_W1),y
            sta  (P8ZP_SCRATCH_W2),y
            dey
            bne  -
            rts

_longcopy
            sta  P8ZP_SCRATCH_B1        ; lsb(count) = remainder in last page
            tya
            tax                         ; x = num pages (1+)
            ldy  #0
-           lda  (P8ZP_SCRATCH_W1),y
            sta  (P8ZP_SCRATCH_W2),y
            iny
            bne  -
            inc  P8ZP_SCRATCH_W1+1
            inc  P8ZP_SCRATCH_W2+1
            dex
            bne  -
            ldy  P8ZP_SCRATCH_B1
            bne  _copyshort
            rts
        }}
    }

    asmsub memset(uword mem @R0, uword numbytes @R1, ubyte value @A) clobbers(A,X,Y) {
        %asm {{
            ldy  cx16.r0
            sty  P8ZP_SCRATCH_W1
            ldy  cx16.r0+1
            sty  P8ZP_SCRATCH_W1+1
            ldx  cx16.r1
            ldy  cx16.r1+1
            jmp  prog8_lib.memset
        }}
    }

    asmsub memsetw(uword mem @R0, uword numwords @R1, uword value @AY) clobbers(A,X,Y) {
        %asm {{
            ldx  cx16.r0
            stx  P8ZP_SCRATCH_W1
            ldx  cx16.r0+1
            stx  P8ZP_SCRATCH_W1+1
            ldx  cx16.r1
            stx  P8ZP_SCRATCH_W2
            ldx  cx16.r1+1
            stx  P8ZP_SCRATCH_W2+1
            jmp  prog8_lib.memsetw
        }}
    }

    inline asmsub read_flags() -> ubyte @A {
        %asm {{
        php
        pla
        }}
    }

    inline asmsub clear_carry() {
        %asm {{
        clc
        }}
    }

    inline asmsub set_carry() {
        %asm {{
        sec
        }}
    }

    inline asmsub clear_irqd() {
        %asm {{
        cli
        }}
    }

    inline asmsub set_irqd() {
        %asm {{
        sei
        }}
    }

    inline asmsub exit(ubyte returnvalue @A) {
        ; -- immediately exit the program with a return code in the A register
        ;    TODO
        %asm {{
            ldx  prog8_lib.orig_stackpointer
            txs
            rts		; return to original caller
        }}
    }

    inline asmsub progend() -> uword @AY {
        %asm {{
            lda  #<prog8_program_end
            ldy  #>prog8_program_end
        }}
    }

}

cx16 {

    ; the sixteen virtual 16-bit registers that the CX16 has defined in the zeropage
    ; they are simulated on the Atari as well but their location in memory is different
    ; TODO
    &uword r0  = $1b00
    &uword r1  = $1b02
    &uword r2  = $1b04
    &uword r3  = $1b06
    &uword r4  = $1b08
    &uword r5  = $1b0a
    &uword r6  = $1b0c
    &uword r7  = $1b0e
    &uword r8  = $1b10
    &uword r9  = $1b12
    &uword r10 = $1b14
    &uword r11 = $1b16
    &uword r12 = $1b18
    &uword r13 = $1b1a
    &uword r14 = $1b1c
    &uword r15 = $1b1e

    &word r0s  = $1b00
    &word r1s  = $1b02
    &word r2s  = $1b04
    &word r3s  = $1b06
    &word r4s  = $1b08
    &word r5s  = $1b0a
    &word r6s  = $1b0c
    &word r7s  = $1b0e
    &word r8s  = $1b10
    &word r9s  = $1b12
    &word r10s = $1b14
    &word r11s = $1b16
    &word r12s = $1b18
    &word r13s = $1b1a
    &word r14s = $1b1c
    &word r15s = $1b1e

    &ubyte r0L  = $1b00
    &ubyte r1L  = $1b02
    &ubyte r2L  = $1b04
    &ubyte r3L  = $1b06
    &ubyte r4L  = $1b08
    &ubyte r5L  = $1b0a
    &ubyte r6L  = $1b0c
    &ubyte r7L  = $1b0e
    &ubyte r8L  = $1b10
    &ubyte r9L  = $1b12
    &ubyte r10L = $1b14
    &ubyte r11L = $1b16
    &ubyte r12L = $1b18
    &ubyte r13L = $1b1a
    &ubyte r14L = $1b1c
    &ubyte r15L = $1b1e

    &ubyte r0H  = $1b01
    &ubyte r1H  = $1b03
    &ubyte r2H  = $1b05
    &ubyte r3H  = $1b07
    &ubyte r4H  = $1b09
    &ubyte r5H  = $1b0b
    &ubyte r6H  = $1b0d
    &ubyte r7H  = $1b0f
    &ubyte r8H  = $1b11
    &ubyte r9H  = $1b13
    &ubyte r10H = $1b15
    &ubyte r11H = $1b17
    &ubyte r12H = $1b19
    &ubyte r13H = $1b1b
    &ubyte r14H = $1b1d
    &ubyte r15H = $1b1f

    &byte r0sL  = $1b00
    &byte r1sL  = $1b02
    &byte r2sL  = $1b04
    &byte r3sL  = $1b06
    &byte r4sL  = $1b08
    &byte r5sL  = $1b0a
    &byte r6sL  = $1b0c
    &byte r7sL  = $1b0e
    &byte r8sL  = $1b10
    &byte r9sL  = $1b12
    &byte r10sL = $1b14
    &byte r11sL = $1b16
    &byte r12sL = $1b18
    &byte r13sL = $1b1a
    &byte r14sL = $1b1c
    &byte r15sL = $1b1e

    &byte r0sH  = $1b01
    &byte r1sH  = $1b03
    &byte r2sH  = $1b05
    &byte r3sH  = $1b07
    &byte r4sH  = $1b09
    &byte r5sH  = $1b0b
    &byte r6sH  = $1b0d
    &byte r7sH  = $1b0f
    &byte r8sH  = $1b11
    &byte r9sH  = $1b13
    &byte r10sH = $1b15
    &byte r11sH = $1b17
    &byte r12sH = $1b19
    &byte r13sH = $1b1b
    &byte r14sH = $1b1d
    &byte r15sH = $1b1f
}
