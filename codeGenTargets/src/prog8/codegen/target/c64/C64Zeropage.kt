package prog8.codegen.target.c64

import prog8.compilerinterface.CompilationOptions
import prog8.compilerinterface.InternalCompilerException
import prog8.compilerinterface.Zeropage
import prog8.compilerinterface.ZeropageType

class C64Zeropage(options: CompilationOptions) : Zeropage(options) {

    override val SCRATCH_B1 = 0x02u      // temp storage for a single byte
    override val SCRATCH_REG = 0x03u     // temp storage for a register, must be B1+1
    override val SCRATCH_W1 = 0xfbu      // temp storage 1 for a word  $fb+$fc
    override val SCRATCH_W2 = 0xfdu      // temp storage 2 for a word  $fd+$fe


    init {
        if (options.floats && options.zeropage !in arrayOf(
                ZeropageType.FLOATSAFE,
                ZeropageType.BASICSAFE,
                ZeropageType.DONTUSE
            ))
            throw InternalCompilerException("when floats are enabled, zero page type should be 'floatsafe' or 'basicsafe' or 'dontuse'")

        if (options.zeropage == ZeropageType.FULL) {
            free.addAll(0x02u..0xffu)
            free.removeAll(setOf(SCRATCH_B1, SCRATCH_REG, SCRATCH_W1, SCRATCH_W1+1u, SCRATCH_W2, SCRATCH_W2+1u))
            free.removeAll(setOf(0xa0u, 0xa1u, 0xa2u, 0x91u, 0xc0u, 0xc5u, 0xcbu, 0xf5u, 0xf6u))        // these are updated by IRQ
        } else {
            if (options.zeropage == ZeropageType.KERNALSAFE || options.zeropage == ZeropageType.FLOATSAFE) {
                free.addAll(listOf(
                        0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11,
                        0x16, 0x17, 0x18, 0x19, 0x1a,
                        0x1b, 0x1c, 0x1d, 0x1e, 0x1f, 0x20, 0x21,
                        0x22, 0x23, 0x24, 0x25,
                        0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46,
                        0x47, 0x48, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x51, 0x52, 0x53,
                        0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60,
                        0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                        0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72,
                        0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c,
                        0x7d, 0x7e, 0x7f, 0x80, 0x81, 0x82, 0x83, 0x84, 0x85, 0x86, 0x87, 0x88, 0x89, 0x8a,
                        0x8b, 0x8c, 0x8d, 0x8e, 0x8f, 0xff
                        // 0x90-0xfa is 'kernal work storage area'
                ).map{it.toUInt()})
            }

            if (options.zeropage == ZeropageType.FLOATSAFE) {
                // remove the zeropage locations used for floating point operations from the free list
                free.removeAll(listOf(
                        0x22, 0x23, 0x24, 0x25,
                        0x10, 0x11, 0x12, 0x26, 0x27, 0x28, 0x29, 0x2a,
                        0x57, 0x58, 0x59, 0x5a, 0x5b, 0x5c, 0x5d, 0x5e, 0x5f, 0x60,
                        0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68,
                        0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 0x70, 0x71, 0x72,
                        0x8b, 0x8c, 0x8d, 0x8e, 0x8f, 0xff
                ).map{it.toUInt()})
            }

            if(options.zeropage!= ZeropageType.DONTUSE) {
                // add the free Zp addresses
                // these are valid for the C-64 but allow BASIC to keep running fully *as long as you don't use tape I/O*
                free.addAll(listOf(0x04, 0x05, 0x06, 0x0a, 0x0e,
                        0x92, 0x96, 0x9b, 0x9c, 0x9e, 0x9f, 0xa5, 0xa6,
                        0xb0, 0xb1, 0xbe, 0xbf, 0xf9).map{it.toUInt()})
            } else {
                // don't use the zeropage at all
                free.clear()
            }
        }

        removeReservedFromFreePool()
    }
}