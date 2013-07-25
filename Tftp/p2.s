.global _exit
.global _open
.global _close
.global _read
.global _write
.global _printf
.global _array

	.align 4
_array:
	.word 16
	.word 15
	.word 14
	.word 13
	.word 12
	.word 11
	.word 10
	.word 9
	.word 8
	.word 7
	.word 6
	.word 5
	.word 4
	.word 3
	.word 2
	.word 1

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
	add r14,r14,#-40
	;; Save Registers 
	sw 0(r14),r3
	sw 4(r14),r4
	sw 8(r14),r5
	sw 12(r14),r6
	sw 16(r14),r7
	sw 20(r14),r8
	sw 24(r14),r9
	addi r3,r0,#0
L2:
	addi r6,r0,#14
		;cmpsi	r3,r6
	sgt	r1,r3,r6
	bnez	r1,L3
	nop
	add r4,r3,#1
L5:
	addi r6,r0,#15
		;cmpsi	r4,r6
	sgt	r1,r4,r6
	bnez	r1,L6
	nop
	lhi r6,(_array>>16)&0xffff
	addui r6,r6,(_array&0xffff)
	slli r7,r3,#2
	add r6,r6,r7
	lhi r7,(_array>>16)&0xffff
	addui r7,r7,(_array&0xffff)
	slli r8,r4,#2
	add r7,r7,r8
	lw r6,0(r6)
	lw r7,0(r7)
		;cmpsi	r6,r7
	sle	r1,r6,r7
	bnez	r1,L8
	nop
	lhi r6,(_array>>16)&0xffff
	addui r6,r6,(_array&0xffff)
	slli r7,r3,#2
	add r6,r6,r7
	lw r5,0(r6)
	lhi r6,(_array>>16)&0xffff
	addui r6,r6,(_array&0xffff)
	slli r7,r3,#2
	add r6,r6,r7
	lhi r7,(_array>>16)&0xffff
	addui r7,r7,(_array&0xffff)
	slli r8,r4,#2
	add r7,r7,r8
	lw r9,0(r7)
	sw 0(r6),r9
	lhi r6,(_array>>16)&0xffff
	addui r6,r6,(_array&0xffff)
	slli r7,r4,#2
	add r6,r6,r7
	sw 0(r6),r5
L8:
L7:
	add r4,r4,#1
	j L5
	nop
L6:
L4:
	add r3,r3,#1
	j L2
	nop
L3:
L1:
	;; Restore the saved registers
	lw r3,-40(r30)
	nop
	lw r4,-36(r30)
	nop
	lw r5,-32(r30)
	nop
	lw r6,-28(r30)
	nop
	lw r7,-24(r30)
	nop
	lw r8,-20(r30)
	nop
	lw r9,-16(r30)
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
