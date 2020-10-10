%import textio
%import conv
%option no_sysinit
%zeropage basicsafe

; Prog8 adaptation of the Text-Elite galaxy system trading simulation engine.
; Original C-version obtained from: http://www.elitehomepage.org/text/index.htm


main {

    const ubyte numforLave = 7      ;  Lave is 7th generated planet in galaxy one
    const ubyte numforZaonce = 129
    const ubyte numforDiso = 147
    const ubyte numforRiedquat = 46

    sub start() {
        txt.lowercase()
        txt.print("\n--> TextElite conversion to Prog8 <--\n")

        galaxy.init(1)
        galaxy.travel_to(numforLave)
        planet.display(false)

        repeat {
            str input = "????????"
            txt.print("\nCommand (?=help): ")
            ubyte num_chars = txt.input_chars(input)
            txt.chrout('\n')
            if num_chars {
                when input[0] {
                    '?' -> {
                        txt.print("\nCommands are:\nbuy   jump      info    cash\nsell  teleport  market  hold\nfuel  galhyp    local   quit\n")
                    }
                    'q' -> break
                    'b' -> trader.do_buy()
                    's' -> trader.do_sell()
                    'f' -> trader.do_fuel()
                    'j' -> trader.do_jump()
                    't' -> trader.do_teleport()
                    'g' -> trader.do_next_galaxy()
                    'i' -> trader.do_info()
                    'm' -> trader.do_show_market()
                    'l' -> trader.do_local()
                    'c' -> trader.do_cash()
                    'h' -> trader.do_hold()
                }
            }
        }
    }
}

trader {
    str input = "????????"
    ubyte num_chars

    sub do_jump() {
        txt.print("\nTODO JUMP\n")
    }

    sub do_teleport() {
        txt.print("\nTODO TELEPORT\n")
    }

    sub do_buy() {
        txt.print("\nTODO BUY\n")
    }

    sub do_sell() {
        txt.print("\nTODO SELL\n")
    }

    sub do_fuel() {
        txt.print("\nBuy fuel. Amount? ")
        void txt.input_chars(input)
        ubyte buy_fuel = lsb(conv.str2uword(input))
        txt.print("TODO\n") ; TODO PURCHASE FUEL
    }

    sub do_cash() {
        txt.print("\nCheat! Set cash amount: ")
        void txt.input_chars(input)
        ship.cash = lsb(conv.str2uword(input))
    }

    sub do_hold() {
        txt.print("\nCheat! TODO adjust cargo hold size\n")
    }

    sub do_next_galaxy() {
        galaxy.nextgalaxy()
        galaxy.travel_to(planet.number)
        planet.display(false)
    }

    sub do_info() {
        txt.print("\nSystem name (empty=current): ")
        num_chars = txt.input_chars(input)
        if num_chars {
            txt.print("\nTODO INFO\n")
        } else {
            planet.display(false)
        }
    }

    sub do_local() {
        galaxy.local_area()
    }

    sub do_show_market() {
        txt.print("\nTODO SHOW MARKET\n")
    }
}

ship {
    const uword Max_fuel = 70

    ubyte fuel = Max_fuel
    uword cash = 1000
}

galaxy {
    const uword GALSIZE = 256
    const uword base0 = $5A4A       ; seeds for the first galaxy
    const uword base1 = $0248
    const uword base2 = $B753

    str pn_pairs = "..lexegezacebisousesarmaindirea.eratenberalavetiedorquanteisrion"

    ubyte number

    uword[3] seed

    sub init(ubyte galaxynum) {
        number = 1
        planet.number = 255
        seed = [base0, base1, base2]
        repeat galaxynum-1 {
            nextgalaxy()
        }
    }

    sub nextgalaxy() {
        seed = [twist(seed[0]), twist(seed[1]), twist(seed[2])]
        number++
        if number==9
            number = 1
    }

    sub travel_to(ubyte system) {
        init(number)
        generate_next_planet()   ; always at least planet 0  (separate to avoid repeat ubyte overflow)
        repeat system {
            generate_next_planet()
        }
        planet.name = make_current_planet_name()
    }

    sub local_area() {
        ubyte current_planet = planet.number
        ubyte px = planet.x
        ubyte py = planet.y
        ubyte pn = 0

        init(number)
        txt.print("\nGalaxy #")
        txt.print_ub(number)
        txt.print(" - systems in vicinity:\n")
        do {
            generate_next_planet()
            ubyte distance = planet.distance(px, py)
            if distance <= ship.Max_fuel {
                if distance <= ship.fuel
                    txt.chrout('*')
                else
                    txt.chrout('-')
                txt.chrout(' ')
                planet.name = make_current_planet_name()
                planet.display(true)
                txt.print(" (")
                util.print_10s(distance)
                txt.print(" LY)\n")
            }
            pn++
        } until pn==0

        travel_to(current_planet)
    }

    ubyte pn_pair1
    ubyte pn_pair2
    ubyte pn_pair3
    ubyte pn_pair4
    ubyte longname

    sub generate_next_planet() {
        determine_planet_properties()
        longname = lsb(seed[0]) & 64

        ; Always four iterations of random number
        pn_pair1 = (msb(seed[2]) & 31) * 2
        tweakseed()
        pn_pair2 = (msb(seed[2]) & 31) * 2
        tweakseed()
        pn_pair3 = (msb(seed[2]) & 31) * 2
        tweakseed()
        pn_pair4 = (msb(seed[2]) & 31) * 2
        tweakseed()
    }

    sub make_current_planet_name() -> str {
        ubyte ni = 0
        str name = "         "    ; max 8
        if pn_pairs[pn_pair1] != '.' {
            name[ni] = pn_pairs[pn_pair1]
            ni++
        }
        if pn_pairs[pn_pair1+1] != '.' {
            name[ni] = pn_pairs[pn_pair1+1]
            ni++
        }
        if pn_pairs[pn_pair2] != '.' {
            name[ni] = pn_pairs[pn_pair2]
            ni++
        }
        if pn_pairs[pn_pair2+1] != '.' {
            name[ni] = pn_pairs[pn_pair2+1]
            ni++
        }
        if pn_pairs[pn_pair3] != '.' {
            name[ni] = pn_pairs[pn_pair3]
            ni++
        }
        if pn_pairs[pn_pair3+1] != '.' {
            name[ni] = pn_pairs[pn_pair3+1]
            ni++
        }

        if longname {
            if pn_pairs[pn_pair4] != '.' {
                name[ni] = pn_pairs[pn_pair4]
                ni++
            }
            if pn_pairs[pn_pair4+1] != '.' {
                name[ni] = pn_pairs[pn_pair4+1]
                ni++
            }
        }

        name[ni] = 0
        return name
    }

    sub determine_planet_properties() {
        ; create the planet's characteristics
        planet.number++
        planet.x = msb(seed[1])
        planet.y = msb(seed[0])
        planet.govtype = lsb(seed[1]) >> 3 & 7  ; bits 3,4 &5 of w1
        planet.economy = msb(seed[0]) & 7  ; bits 8,9 &A of w0
        if planet.govtype <= 1
            planet.economy = (planet.economy | 2)
        planet.techlevel = (msb(seed[1]) & 3) + (planet.economy ^ 7)
        planet.techlevel += planet.govtype >> 1
        if planet.govtype & 1
            planet.techlevel++
        planet.population = 4 * planet.techlevel + planet.economy
        planet.population += planet.govtype + 1
        planet.productivity = ((planet.economy ^ 7) + 3) * (planet.govtype + 4)
        planet.productivity *= planet.population * 8
        ubyte seed2_msb = msb(seed[2])
        planet.radius = mkword((seed2_msb & 15) + 11, planet.x)
        planet.species_is_alien = lsb(seed[2]) & 128       ; bit 7 of w2_lo
        if planet.species_is_alien {
            planet.species_size = (seed2_msb >> 2) & 7      ; bits 2-4 of w2_hi
            planet.species_color = seed2_msb >> 5           ; bits 5-7 of w2_hi
            planet.species_look = (seed2_msb ^ msb(seed[1])) & 7   ;bits 0-2 of (w0_hi EOR w1_hi)
            planet.species_kind = (planet.species_look + (seed2_msb & 3)) & 7      ;Add bits 0-1 of w2_hi to A from previous step, and take bits 0-2 of the result
        }

        planet.goatsoup_seed = [lsb(seed[1]), msb(seed[1]), lsb(seed[2]), seed2_msb]
    }

    sub tweakseed() {
        uword temp = seed[0] + seed[1] + seed[2]
        seed[0] = seed[1]
        seed[1] = seed[2]
        seed[2] = temp
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
}

planet {
    %option force_output

    str[] species_sizes = ["Large", "Fierce", "Small"]
    str[] species_colors = ["Green", "Red", "Yellow", "Blue", "Black", "Harmless"]
    str[] species_looks = ["Slimy", "Bug-Eyed", "Horned", "Bony", "Fat", "Furry"]
    str[] species_kinds = ["Rodents", "Frogs", "Lizards", "Lobsters", "Birds", "Humanoids", "Felines", "Insects"]
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

    str name = "        "       ; 8 max
    ubyte number                ; starts at 0 in new galaxy, then increases by 1 for each generated planet
    ubyte x
    ubyte y
    ubyte economy
    ubyte govtype
    ubyte techlevel
    ubyte population
    uword productivity
    uword radius
    ubyte species_is_alien      ; otherwise "Human Colonials"
    ubyte species_size
    ubyte species_color
    ubyte species_look
    ubyte species_kind

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

    sub distance(ubyte px, ubyte py) -> ubyte {
        uword ax
        uword ay
        if px>x
            ax=px-x
        else
            ax=x-px
        if py>y
            ay=py-y
        else
            ay=y-py
        ay /= 2
        ubyte d = sqrt16(ax*ax + ay*ay)
        if d>63
            return 255
        return d*4
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
            txt.chrout('\'')
            txt.print_ub(y)
            txt.chrout(' ')
            txt.chrout('#')
            txt.print_ub(number)
            txt.print("\nEconomy: ")
            txt.print(econnames[economy])
            txt.print("\nGovernment: ")
            txt.print(govnames[govtype])
            txt.print("\nTech Level: ")
            txt.print_ub(techlevel+1)
            txt.print("\nTurnover: ")
            txt.print_uw(productivity)
            txt.print("\nRadius: ")
            txt.print_uw(radius)
            txt.print("\nPopulation: ")
            txt.print_ub(population >> 3)
            txt.print(" Billion\nSpecies: ")
            if species_is_alien {
                if species_size < len(species_sizes) {
                    txt.print(species_sizes[species_size])
                    txt.chrout(' ')
                }
                if species_color < len(species_colors) {
                    txt.print(species_colors[species_color])
                    txt.chrout(' ')
                }
                if species_look < len(species_looks) {
                    txt.print(species_looks[species_look])
                    txt.chrout(' ')
                }
                if species_kind < len(species_kinds) {
                    txt.print(species_kinds[species_kind])
                }
            } else {
                txt.print("Human Colonials")
            }
            txt.chrout('\n')
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

util {
    asmsub print_10s(ubyte value @A) clobbers(A, X, Y) {
        %asm {{
		    jsr  conv.ubyte2decimal         ;(100s in Y, 10s in A, 1s in X)
		    pha
		    cpy  #'0'
		    beq  +
		    tya
		    jsr  c64.CHROUT
+           pla
            jsr  c64.CHROUT
            lda  #'.'
            jsr  c64.CHROUT
            txa
            jsr  c64.CHROUT
		    rts
        }}
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
_saveX      .byte 0
        }}
    }

}
