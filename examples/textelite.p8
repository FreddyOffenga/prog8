%import textio
%zeropage basicsafe
%option no_sysinit

main {
    sub start() {
        txt.lowercase()

        txt.print("\n--> TextElite conversion to Prog8 <--\n\n")

        galaxy.init(1)
        txt.print("\nstart!!!!")
        galaxy.debug_seed()

        galaxy.generate_planet(galaxy.numforLave)
        planet.display(false)

;        repeat 5 {
;            planet.set_seed(rndw(), rndw())
;            planet.name = planet.random_name()
;            planet.display(false)
;            txt.chrout('\n')
;        }
    }

    asmsub testX() {
        %asm {{
            stx  _saveX
            lda  #13
            jsr  txt.chrout
            lda  _saveX
            jsr  txt.print_ub
            lda  #13
            jsr  txt.chrout
            ldx  _saveX
            rts
_saveX   .byte 0
        }}
    }

}

galaxy {
    const uword GALSIZE = 256
    ; seed for first galaxy:
    const uword base0 = $5A4A
    const uword base1 = $0248
    const uword base2 = $B753
    const ubyte numforLave = 7  ;  Lave is 7th generated planet in galaxy one
    const ubyte numforZaonce = 129
    const ubyte numforDiso = 147
    const ubyte numforRied = 46

    str pairs = "..lexegezacebisousesarmaindirea.eratenberalavetiedorquanteisrion"

    ubyte number
    uword[3] seed

    sub init(ubyte galaxynum) {
        number = galaxynum
        ; TODO make this work:  seed = [base0, base1, base2]
        seed[0] = base0
        seed[1] = base1
        seed[2] = base2

        repeat galaxynum-1 {
            nextgalaxy()
        }
        ; planets are now created procedurally on the fly.
    }

    sub nextgalaxy() {
        ; Apply to base seed; once for galaxy 2
        ; twice for galaxy 3, etc.
        ; Eighth application gives galaxy 1 again
        seed[0] = twist(seed[0])
        seed[1] = twist(seed[1])
        seed[2] = twist(seed[2])
    }

    sub twist(uword x) -> uword {
        ubyte xh = msb(x)
        ubyte xl = lsb(x)
        rol(xh)
        rol(xl)
        return mkword(xh, xl)
    }

    sub debug_seed() {
        txt.print("\ngalaxy #")
        txt.print_ub(number)
        txt.print("\ngalaxy seed0=")
        txt.print_uwhex(galaxy.seed[0], true)
        txt.print("\ngalaxy seed1=")
        txt.print_uwhex(galaxy.seed[1], true)
        txt.print("\ngalaxy seed2=")
        txt.print_uwhex(galaxy.seed[2], true)
        txt.chrout('\n')
    }

    sub generate_planet(ubyte planetnum) {
        ubyte pair1
        ubyte pair2
        ubyte pair3
        ubyte pair4
        repeat planetnum+1 {
            ; Always four iterations of random number
            pair1 = (msb(seed[2]) & 31) * 2
            tweakseed()
            pair2 = (msb(seed[2]) & 31) * 2
            tweakseed()
            pair3 = (msb(seed[2]) & 31) * 2
            tweakseed()
            pair4 = (msb(seed[2]) & 31) * 2
            tweakseed()
        }

        debug_seed()

        txt.print("generating planet #")
        txt.print_ub(planetnum)
        txt.print("\npairs:")
        txt.print_ub(pair1)
        txt.chrout(',')
        txt.print_ub(pair2)
        txt.chrout(',')
        txt.print_ub(pair3)
        txt.chrout(',')
        txt.print_ub(pair4)
        txt.chrout('\n')

        ubyte ni = 0
        if pairs[pair1] != '.' {
            planet.name[ni] = pairs[pair1]
            ni++
        }
        if pairs[pair1+1] != '.' {
            planet.name[ni] = pairs[pair1+1]
            ni++
        }
        if pairs[pair2] != '.' {
            planet.name[ni] = pairs[pair2]
            ni++
        }
        if pairs[pair2+1] != '.' {
            planet.name[ni] = pairs[pair2+1]
            ni++
        }
        if pairs[pair3] != '.' {
            planet.name[ni] = pairs[pair3]
            ni++
        }
        if pairs[pair3+1] != '.' {
            planet.name[ni] = pairs[pair1+3]
            ni++
        }

        if lsb(seed[0]) & 64 {
            ; long name
            if pairs[pair4] != '.' {
                planet.name[ni] = pairs[pair4]
                ni++
            }
            if pairs[pair4+1] != '.' {
                planet.name[ni] = pairs[pair4+1]
                ni++
            }
        }

        planet.name[ni] = 0
    }

    sub tweakseed() {
        uword temp = seed[0] + seed[1] + seed[2]
        seed[0] = seed[1]
        seed[1] = seed[2]
        seed[2] = temp
    }
}

planet {
    %option force_output

    str[] govnames = ["Anarchy", "Feudal", "Multi-gov", "Dictatorship", "Communist", "Confederacy", "Democracy", "Corporate State"]
    str[] econnames = ["Rich Industrial", "Average Industrial", "Poor Industrial", "Mainly Industrial",
                       "Mainly Agricultural", "Rich Agricultural", "Average Agricultural", "Poor Agricultural"]

    str[] words81 = ["fabled", "notable", "well known", "famous", "noted"]
    str[] words82 = ["very", "mildly", "most", "reasonably", ""]
    str[] words83 = ["ancient", "\x95", "great", "vast", "pink"]
    str[] words84 = ["\x9E \x9D plantations", "mountains", "\x9C", "\x94 forests", "oceans"]
    str[] words85 = ["shyness", "silliness", "mating traditions", "loathing of \x86", "love for \x86"]
    str[] words86 = ["food blenders", "tourists", "poetry", "discos", "\x8E"]
    str[] words87 = ["talking tree", "crab", "bat", "lobst", "\xB2"]
    str[] words88 = ["beset", "plagued", "ravaged", "cursed", "scourged"]
    str[] words89 = ["\x96 civil war", "\x9B \x98 \x99s", "a \x9B disease", "\x96 earthquakes", "\x96 solar activity"]
    str[] words8A = ["its \x83 \x84", "the \xB1 \x98 \x99", "its inhabitants' \x9A \x85", "\xA1", "its \x8D \x8E"]
    str[] words8B = ["juice", "brandy", "water", "brew", "gargle blasters"]
    str[] words8C = ["\xB2", "\xB1 \x99", "\xB1 \xB2", "\xB1 \x9B", "\x9B \xB2"]
    str[] words8D = ["fabulous", "exotic", "hoopy", "unusual", "exciting"]
    str[] words8E = ["cuisine", "night life", "casinos", "sit coms", " \xA1 "]
    str[] words8F = ["\xB0", "The planet \xB0", "The world \xB0", "This planet", "This world"]
    str[] words90 = ["n unremarkable", " boring", " dull", " tedious", " revolting"]
    str[] words91 = ["planet", "world", "place", "little planet", "dump"]
    str[] words92 = ["wasp", "moth", "grub", "ant", "\xB2"]
    str[] words93 = ["poet", "arts graduate", "yak", "snail", "slug"]
    str[] words94 = ["tropical", "dense", "rain", "impenetrable", "exuberant"]
    str[] words95 = ["funny", "wierd", "unusual", "strange", "peculiar"]
    str[] words96 = ["frequent", "occasional", "unpredictable", "dreadful", "deadly"]
    str[] words97 = ["\x82 \x81 for \x8A", "\x82 \x81 for \x8A and \x8A", "\x88 by \x89", "\x82 \x81 for \x8A but \x88 by \x89", "a\x90 \x91"]
    str[] words98 = ["\x9B", "mountain", "edible", "tree", "spotted"]
    str[] words99 = ["\x9F", "\xA0", "\x87oid", "\x93", "\x92"]
    str[] words9A = ["ancient", "exceptional", "eccentric", "ingrained", "\x95"]
    str[] words9B = ["killer", "deadly", "evil", "lethal", "vicious"]
    str[] words9C = ["parking meters", "dust clouds", "ice bergs", "rock formations", "volcanoes"]
    str[] words9D = ["plant", "tulip", "banana", "corn", "\xB2weed"]
    str[] words9E = ["\xB2", "\xB1 \xB2", "\xB1 \x9B", "inhabitant", "\xB1 \xB2"]
    str[] words9F = ["shrew", "beast", "bison", "snake", "wolf"]
    str[] wordsA0 = ["leopard", "cat", "monkey", "goat", "fish"]
    str[] wordsA1 = ["\x8C \x8B", "\xB1 \x9F \xA2", "its \x8D \xA0 \xA2", "\xA3 \xA4", "\x8C \x8B"]
    str[] wordsA2 = ["meat", "cutlet", "steak", "burgers", "soup"]
    str[] wordsA3 = ["ice", "mud", "Zero-G", "vacuum", "\xB1 ultra"]
    str[] wordsA4 = ["hockey", "cricket", "karate", "polo", "tennis"]

    uword[] wordlists = [
        words81, words82, words83, words84, words85, words86, words87, words88,
        words89, words8A, words8B, words8C, words8D, words8E, words8F, words90,
        words91, words92, words93, words94, words95, words96, words97, words98,
        words99, words9A, words9B, words9C, words9D, words9E, words9F, wordsA0,
        wordsA1, wordsA2, wordsA3, wordsA4]

    str pairs0 = "abouseitiletstonlonuthnoallexegezacebisousesarmaindirea.eratenbe"

    ubyte[4] goatsoup_rnd = [0, 0, 0, 0]
    ubyte[4] goatsoup_seed = [0, 0, 0, 0]

    str name = "        "        ; 8 max
    ubyte x
    ubyte y
    ubyte economy
    ubyte govtype
    ubyte techlevel
    ubyte population
    ubyte productivity
    ubyte radius
    ; todo: species

    sub set_seed(uword s1, uword s2) {
        goatsoup_seed[0] = lsb(s1)
        goatsoup_seed[1] = msb(s1)
        goatsoup_seed[2] = lsb(s2)
        goatsoup_seed[3] = msb(s2)
        reset_rnd()
    }

    sub reset_rnd() {
        goatsoup_rnd[0] = goatsoup_seed[0]
        goatsoup_rnd[1] = goatsoup_seed[1]
        goatsoup_rnd[2] = goatsoup_seed[2]
        goatsoup_rnd[3] = goatsoup_seed[3]
    }

    sub random_name() -> str {
        ubyte ii
        str name = "        "       ; 8 chars max
        ubyte nx = 0
        for ii in 0 to gen_rnd_number() & 3 {
            ubyte x = gen_rnd_number() & $3e
            if pairs0[x] != '.' {
                name[nx] = pairs0[x]
                nx++
            }
            if pairs0[x+1] != '.' {
                name[nx] = pairs0[x+1]
                nx++
            }
        }
        name[nx] = 0
        name[0] |= 32       ; uppercase first letter
        return name
    }

    sub gen_rnd_number() -> ubyte {
        ubyte x = goatsoup_rnd[0] * 2
        uword a = x as uword + goatsoup_rnd[2]
        if goatsoup_rnd[0] > 127
            a ++
        goatsoup_rnd[0] = lsb(a)
        goatsoup_rnd[2] = x
        x = goatsoup_rnd[1]
        ubyte ac = x + goatsoup_rnd[3] + msb(a)
        goatsoup_rnd[1] = ac
        goatsoup_rnd[3] = x
        return ac
    }

    sub soup() -> str {
        str planet_result = " " * 160
        uword[6] source_stack
        ubyte stack_ptr = 0
        str start_source = "\x8F is \x97."
        uword source_ptr = &start_source
        uword result_ptr = &planet_result

        reset_rnd()
        recursive_soup()
        return planet_result

        sub recursive_soup() {
            repeat {
                ubyte c = @(source_ptr)
                source_ptr++
                if c == $00 {
                    @(result_ptr) = 0
                    return
                }
                else if c <= $80 {
                    @(result_ptr) = c
                    result_ptr++
                }
                else {
                    if c <= $a4 {
                        ubyte rnr = gen_rnd_number()
                        ubyte wordNr = (rnr >= $33) + (rnr >= $66) + (rnr >= $99) + (rnr >= $CC)
                        source_stack[stack_ptr] = source_ptr
                        stack_ptr++
                        source_ptr = getword(c, wordNr)
                        ; TODO recursive call... should give error message... but hey since it's not doing that here now, lets exploit it
                        recursive_soup()         ; RECURSIVE CALL
                        stack_ptr--
                        source_ptr = source_stack[stack_ptr]
                    } else {
                        if c == $b0 {
                            @(result_ptr) = name[0] | 32
                            result_ptr++
                            concat_string(&name + 1)
                        }
                        else if c == $b1 {
                            @(result_ptr) = name[0] | 32
                            result_ptr++
                            ubyte ni
                            for ni in 1 to len(name) {
                                ubyte cc = name[ni]
                                if cc=='e' or cc=='o' or cc==0
                                    break
                                else {
                                    @(result_ptr) = cc
                                    result_ptr++
                                }
                            }
                            @(result_ptr) = 'i'
                            result_ptr++
                            @(result_ptr) = 'a'
                            result_ptr++
                            @(result_ptr) = 'n'
                            result_ptr++
                        }
                        else if c == $b2 {
                            concat_string(random_name())
                        }
                        else {
                            @(result_ptr) = c
                            result_ptr++
                        }
                    }
                }
            }
        }

        sub concat_string(uword str_ptr) {
            repeat {
                ubyte c = @(str_ptr)
                if c==0
                    break
                else {
                    @(result_ptr) = c
                    str_ptr++
                    result_ptr++
                }
            }
        }
    }

    sub display(ubyte compressed) {
        if compressed {
            print_name_uppercase()
            txt.print(" TL:")
            txt.print_ub(techlevel+1)
            txt.chrout(' ')
            txt.print(econnames[economy])
            txt.chrout(' ')
            txt.print(govnames[govtype])
        } else {
            txt.print("\n\nSystem: ")
            print_name_uppercase()
            txt.print("\nPosition: ")
            txt.print_ub(x)
            txt.chrout(',')
            txt.print_ub(y)
            txt.print("\nEconomy: ")
            txt.print(econnames[economy])
            txt.print("\nGovernment: ")
            txt.print(govnames[govtype])
            txt.print("\nTech Level: ")
            txt.print_ub(techlevel+1)
            txt.print("\nTurnover: ")
            txt.print_ub(productivity)
            txt.print("\nRadius:")
            txt.print_ub(radius)
            txt.print("\nPopulation: ")
            txt.print_ub(population >> 3)
            txt.print(" Billion\n")
            txt.print(soup())
            txt.chrout('\n')
        }
    }

    sub print_name_uppercase() {
        ubyte c
        for c in name
            txt.chrout(c | 32)
    }

    asmsub getword(ubyte list @A, ubyte wordidx @Y) -> uword @AY {
        %asm {{
            sty  P8ZP_SCRATCH_REG
            sec
            sbc  #$81
            asl  a
            tay
            lda  wordlists,y
            sta  P8ZP_SCRATCH_W1
            lda  wordlists+1,y
            sta  P8ZP_SCRATCH_W1+1
            lda  P8ZP_SCRATCH_REG
            asl  a
            tay
            lda  (P8ZP_SCRATCH_W1),y
            pha
            iny
            lda  (P8ZP_SCRATCH_W1),y
            tay
            pla
            rts
        }}
    }
}
