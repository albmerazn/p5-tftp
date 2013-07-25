.global _exit
.global _open
.global _close
.global _read
.global _write
.global _printf

	.align 8
LC0:
	.double 100.00000000000000000000
	.align 8
LC1:
	.double 10.00000000000000000000
	.align 8
LC2:
	.double 0.00000000000000000000
	.align 4
.global _main
_main:
	;; Initialize Stack Pointer
	add r14,r0,r0
	lhi r14, ((memSize-4)>>16)&0xffff
	addui r14, r14, ((memSize-4)&0xffff)
	;; Save the old frame pointer 
	sw -4(r14),r30
	;; Save the return address 
	sw -8(r14),r31
	;; Establish new frame pointer 
	add r30,r0,r14
	;; Adjust Stack Pointer 
	add r14,r14,#-80
	;; Save Registers 
	sw 0(r14),r3
	sw 4(r14),r4
	sw 8(r14),r5
	sw 12(r14),r6
	sw 16(r14),r7
	sw 20(r14),r8
	sw 24(r14),r9
	sw 28(r14),r10
	sw 32(r14),r11
	sw 36(r14),r12
	sd 40(r14),f4
	sd 48(r14),f6
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	sw 0(r3),r0
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	lw r4,0(r3)
	addi r3,r0,#24
	sgt	r1,r4,r3
	bnez r1,L27
	nop
	lhi r6,(_i>>16)&0xffff
	addui r6,r6,(_i&0xffff)
	lhi r7,(_j>>16)&0xffff
	addui r7,r7,(_j&0xffff)
	addi r9,r0,#25
	lhi r8,(_a>>16)&0xffff
	addui r8,r8,(_a&0xffff)
L10:
	sw 0(r6),r0
	j L28
	nop
L9:
	lw r4,0(r7)
	movi2fp f2,r4
	movi2fp f3,r9
	mult f2,f2,f3
	movfp2i r3,f2
	lw r5,0(r6)
	add r3,r3,r5
	slli r3,r3,#3
	add r3,r8,r3
	ld f0,0(r6)
	cvti2d f4,f0
	ld f0,0(r7)
	cvti2d f6,f0
	multd f4,f4,f6
	add r1,r0,LC0
	ld f6,0(r1)
	divd f6,f4,f6
	sd 0(r3),f6
	seq	r1,r5,r4
	beqz r1,L7
	nop
	add r1,r0,LC1
	ld f4,0(r1)
	multd f4,f6,f4
	sd 0(r3),f4
L7:
	lw r3,0(r6)
	add r3,r3,#1
	sw 0(r6),r3
L28:
	lw r4,0(r6)
	addi r3,r0,#24
	sle	r1,r4,r3
	bnez r1,L9
	nop
	lw r3,0(r7)
	add r3,r3,#1
	sw 0(r7),r3
	lw r4,0(r7)
	addi r3,r0,#24
	sle	r1,r4,r3
	bnez r1,L10
	nop
L27:
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	sw 0(r3),r0
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	lw r3,0(r3)
	addi r5,r0,#24
	sgt	r1,r3,r5
	bnez r1,L25
	nop
	lhi r6,(_j>>16)&0xffff
	addui r6,r6,(_j&0xffff)
	lhi r8,(_rhs>>16)&0xffff
	addui r8,r8,(_rhs&0xffff)
	lhi r7,(LC2>>16)&0xffff
	addui r7,r7,(LC2&0xffff)
	addi r5,r0,#24
L14:
	lw r3,0(r6)
	slli r4,r3,#3
	add r4,r8,r4
	lw r11,0(r7)
	nop
	lw r12,4(r7)
	nop
	sw 0(r4),r11
	sw 4(r4),r12
	add r3,r3,#1
	sw 0(r6),r3
	lw r3,0(r6)
	sle	r1,r3,r5
	bnez r1,L14
	nop
L25:
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	sw 0(r3),r0
	lhi r3,(_j>>16)&0xffff
	addui r3,r3,(_j&0xffff)
	lw r4,0(r3)
	addi r3,r0,#24
	sgt	r1,r4,r3
	bnez r1,L24
	nop
	lhi r7,(_i>>16)&0xffff
	addui r7,r7,(_i&0xffff)
	lhi r10,(_rhs>>16)&0xffff
	addui r10,r10,(_rhs&0xffff)
	lhi r9,(_a>>16)&0xffff
	addui r9,r9,(_a&0xffff)
	lhi r8,(_j>>16)&0xffff
	addui r8,r8,(_j&0xffff)
L22:
	sw 0(r7),r0
	j L29
	nop
L21:
	lw r4,0(r7)
	slli r5,r4,#3
	add r5,r10,r5
	lw r3,0(r8)
	addi r6,r0,#25
	movi2fp f2,r3
	movi2fp f3,r6
	mult f2,f2,f3
	movfp2i r3,f2
	add r3,r3,r4
	slli r3,r3,#3
	add r3,r9,r3
	ld f4,0(r5)
	nop
	ld f6,0(r3)
	nop
	addd f4,f4,f6
	sd 0(r5),f4
	add r4,r4,#1
	sw 0(r7),r4
L29:
	lw r4,0(r7)
	addi r3,r0,#24
	sle	r1,r4,r3
	bnez r1,L21
	nop
	lw r3,0(r8)
	add r3,r3,#1
	sw 0(r8),r3
	lw r4,0(r8)
	addi r3,r0,#24
	sle	r1,r4,r3
	bnez r1,L22
	nop
L24:
	;; Restore the saved registers
	lw r3,-64(r30)
	nop
	lw r4,-60(r30)
	nop
	lw r5,-56(r30)
	nop
	lw r6,-52(r30)
	nop
	lw r7,-48(r30)
	nop
	lw r8,-44(r30)
	nop
	lw r9,-40(r30)
	nop
	lw r10,-36(r30)
	nop
	lw r11,-32(r30)
	nop
	lw r12,-28(r30)
	nop
	ld f4,-24(r30)
	nop
	ld f6,-16(r30)
	nop
	;; Restore return address
	lw r31,-8(r30)
	nop
	;; Restore stack pointer
	add r14,r0,r30
	;; Restore frame pointer
	lw r30,-4(r30)
	nop
	;; HALT
	jal _exit
	nop

_exit:
	trap #0
	jr r31
	nop
_open:
	trap #1
	jr r31
	nop
_close:
	trap #2
	jr r31
	nop
_read:
	trap #3
	jr r31
	nop
_write:
	trap #4
	jr r31
	nop
_printf:
	trap #5
	jr r31
	nop
.global _j
_j:	.space 8
.global _i
_i:	.space 8
.global _a
_a:	.space 5000
.global _rhs
_rhs:	.space 200
