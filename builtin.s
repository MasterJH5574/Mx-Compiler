	.text
	.file	"builtin-function_str.c"
	.globl	print                   # -- Begin function print
	.p2align	2
	.type	print,@function
print:                                  # @print
# %bb.0:
	lui	a1, %hi(.L.str)
	addi	a1, a1, %lo(.L.str)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end0:
	.size	print, .Lfunc_end0-print
                                        # -- End function
	.globl	println                 # -- Begin function println
	.p2align	2
	.type	println,@function
println:                                # @println
# %bb.0:
	tail	puts
.Lfunc_end1:
	.size	println, .Lfunc_end1-println
                                        # -- End function
	.globl	printInt                # -- Begin function printInt
	.p2align	2
	.type	printInt,@function
printInt:                               # @printInt
# %bb.0:
	lui	a1, %hi(.L.str.2)
	addi	a1, a1, %lo(.L.str.2)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end2:
	.size	printInt, .Lfunc_end2-printInt
                                        # -- End function
	.globl	printlnInt              # -- Begin function printlnInt
	.p2align	2
	.type	printlnInt,@function
printlnInt:                             # @printlnInt
# %bb.0:
	lui	a1, %hi(.L.str.3)
	addi	a1, a1, %lo(.L.str.3)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end3:
	.size	printlnInt, .Lfunc_end3-printlnInt
                                        # -- End function
	.globl	getString               # -- Begin function getString
	.p2align	2
	.type	getString,@function
getString:                              # @getString
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	.cfi_offset ra, -4
	.cfi_offset s0, -8
	addi	a0, zero, 257
	mv	a1, zero
	call	malloc
	mv	s0, a0
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	mv	a1, s0
	call	__isoc99_scanf
	mv	a0, s0
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end4:
	.size	getString, .Lfunc_end4-getString
	.cfi_endproc
                                        # -- End function
	.globl	getInt                  # -- Begin function getInt
	.p2align	2
	.type	getInt,@function
getInt:                                 # @getInt
	.cfi_startproc
# %bb.0:
	addi	sp, sp, -16
	.cfi_def_cfa_offset 16
	sw	ra, 12(sp)
	.cfi_offset ra, -4
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	addi	a1, sp, 8
	call	__isoc99_scanf
	lw	a0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end5:
	.size	getInt, .Lfunc_end5-getInt
	.cfi_endproc
                                        # -- End function
	.globl	toString                # -- Begin function toString
	.p2align	2
	.type	toString,@function
toString:                               # @toString
# %bb.0:
	addi	sp, sp, -48
	sw	ra, 44(sp)
	sw	s0, 40(sp)
	sw	s1, 36(sp)
	sw	s2, 32(sp)
	sw	s3, 28(sp)
	sw	s4, 24(sp)
	sw	s5, 20(sp)
	beqz	a0, .LBB6_4
# %bb.1:
	mv	s5, a0
	srai	a0, a0, 31
	add	a1, s5, a0
	xor	s0, a1, a0
	addi	a1, zero, 1
	slti	s3, s5, 1
	blt	s0, a1, .LBB6_5
# %bb.2:                                # %.preheader.preheader
	mv	a5, zero
	lui	a1, 838861
	addi	a1, a1, -819
	addi	a6, zero, -10
	addi	a3, sp, 10
	addi	a4, zero, 9
.LBB6_3:                                # %.preheader
                                        # =>This Inner Loop Header: Depth=1
	mv	a0, s0
	mulhu	s1, s0, a1
	srli	s0, s1, 3
	mul	s1, s0, a6
	add	a2, s1, a0
	addi	s1, a5, 1
	slli	a5, a5, 24
	srai	a5, a5, 24
	add	a5, a3, a5
	sb	a2, 0(a5)
	mv	a5, s1
	bltu	a4, a0, .LBB6_3
	j	.LBB6_6
.LBB6_4:
	addi	a0, zero, 2
	mv	a1, zero
	call	malloc
	addi	a1, zero, 48
	sb	a1, 0(a0)
	sb	zero, 1(a0)
	j	.LBB6_20
.LBB6_5:
	mv	s1, zero
.LBB6_6:                                # %.loopexit5
	slli	a0, s1, 24
	srai	s4, a0, 24
	add	s2, s4, s3
	addi	a0, s2, 1
	srai	a1, a0, 31
	call	malloc
	bgtz	s5, .LBB6_8
# %bb.7:
	addi	a1, zero, 45
	sb	a1, 0(a0)
.LBB6_8:
	addi	a1, zero, 1
	blt	s4, a1, .LBB6_19
# %bb.9:
	andi	t0, s1, 255
	addi	a1, zero, 32
	addi	t1, s4, -1
	bgeu	t0, a1, .LBB6_11
# %bb.10:
	mv	s4, zero
	mv	a1, zero
	j	.LBB6_17
.LBB6_11:
	andi	s4, s1, 224
	addi	a2, s4, -32
	sltu	a1, a2, s4
	addi	a1, a1, -1
	slli	a4, a1, 27
	srli	a5, a2, 5
	or	s1, a5, a4
	addi	a4, s1, 1
	or	a2, a2, a1
	andi	a6, a4, 1
	beqz	a2, .LBB6_21
# %bb.12:
	mv	t6, zero
	mv	t5, zero
	srli	a1, a1, 5
	sltu	s1, a4, s1
	add	s1, a1, s1
	sub	t3, a4, a6
	sltu	a4, a4, a6
	sub	t2, s1, a4
	addi	a7, sp, 10
.LBB6_13:                               # =>This Inner Loop Header: Depth=1
	sub	a4, t1, t6
	add	s1, a7, a4
	lb	a1, -15(s1)
	lb	s0, -14(s1)
	lb	t4, -13(s1)
	addi	a1, a1, 48
	or	a4, t6, s3
	add	a4, a0, a4
	sb	a1, 15(a4)
	lb	a1, -12(s1)
	addi	s0, s0, 48
	sb	s0, 14(a4)
	lb	s0, -11(s1)
	addi	a5, t4, 48
	sb	a5, 13(a4)
	lb	a5, -10(s1)
	addi	a1, a1, 48
	sb	a1, 12(a4)
	lb	a1, -9(s1)
	addi	s0, s0, 48
	sb	s0, 11(a4)
	lb	s0, -8(s1)
	addi	a5, a5, 48
	sb	a5, 10(a4)
	lb	a5, -7(s1)
	addi	a1, a1, 48
	sb	a1, 9(a4)
	lb	a1, -6(s1)
	addi	s0, s0, 48
	sb	s0, 8(a4)
	lb	s0, -5(s1)
	addi	a5, a5, 48
	sb	a5, 7(a4)
	lb	a5, -4(s1)
	addi	a1, a1, 48
	sb	a1, 6(a4)
	lb	a1, -3(s1)
	addi	s0, s0, 48
	sb	s0, 5(a4)
	lb	s0, -2(s1)
	addi	a5, a5, 48
	sb	a5, 4(a4)
	lb	a5, -1(s1)
	addi	a1, a1, 48
	sb	a1, 3(a4)
	lb	a1, 0(s1)
	addi	s0, s0, 48
	sb	s0, 2(a4)
	lb	s0, -31(s1)
	addi	a5, a5, 48
	sb	a5, 1(a4)
	lb	a5, -30(s1)
	addi	a1, a1, 48
	sb	a1, 0(a4)
	lb	a1, -29(s1)
	addi	s0, s0, 48
	sb	s0, 31(a4)
	lb	s0, -28(s1)
	addi	a5, a5, 48
	sb	a5, 30(a4)
	lb	a5, -27(s1)
	addi	a1, a1, 48
	sb	a1, 29(a4)
	lb	a1, -26(s1)
	addi	s0, s0, 48
	sb	s0, 28(a4)
	lb	s0, -25(s1)
	addi	a5, a5, 48
	sb	a5, 27(a4)
	lb	a5, -24(s1)
	addi	a1, a1, 48
	sb	a1, 26(a4)
	lb	a1, -23(s1)
	addi	s0, s0, 48
	sb	s0, 25(a4)
	lb	s0, -22(s1)
	addi	a5, a5, 48
	sb	a5, 24(a4)
	lb	a5, -21(s1)
	addi	a1, a1, 48
	sb	a1, 23(a4)
	lb	a1, -20(s1)
	addi	s0, s0, 48
	sb	s0, 22(a4)
	lb	s0, -18(s1)
	addi	a5, a5, 48
	sb	a5, 21(a4)
	lb	a5, -17(s1)
	addi	a1, a1, 48
	sb	a1, 20(a4)
	lb	a1, -19(s1)
	lb	a2, -16(s1)
	addi	a5, a5, 48
	addi	s1, s0, 48
	addi	a1, a1, 48
	sb	a1, 19(a4)
	sb	s1, 18(a4)
	sb	a5, 17(a4)
	ori	s0, t6, 32
	sub	a1, t1, s0
	add	s1, a7, a1
	lb	a1, -15(s1)
	addi	a2, a2, 48
	sb	a2, 16(a4)
	lb	a4, -14(s1)
	addi	a1, a1, 48
	or	a2, s0, s3
	add	a2, a0, a2
	sb	a1, 15(a2)
	lb	a1, -13(s1)
	addi	a4, a4, 48
	sb	a4, 14(a2)
	lb	a4, -12(s1)
	addi	a1, a1, 48
	sb	a1, 13(a2)
	lb	a1, -11(s1)
	addi	a4, a4, 48
	sb	a4, 12(a2)
	lb	a4, -10(s1)
	addi	a1, a1, 48
	sb	a1, 11(a2)
	lb	a1, -9(s1)
	addi	a4, a4, 48
	sb	a4, 10(a2)
	lb	a4, -8(s1)
	addi	a1, a1, 48
	sb	a1, 9(a2)
	lb	a1, -7(s1)
	addi	a4, a4, 48
	sb	a4, 8(a2)
	lb	a4, -6(s1)
	addi	a1, a1, 48
	sb	a1, 7(a2)
	lb	a1, -5(s1)
	addi	a4, a4, 48
	sb	a4, 6(a2)
	lb	a4, -4(s1)
	addi	a1, a1, 48
	sb	a1, 5(a2)
	lb	a1, -3(s1)
	addi	a4, a4, 48
	sb	a4, 4(a2)
	lb	a4, -2(s1)
	addi	a1, a1, 48
	sb	a1, 3(a2)
	lb	a1, -1(s1)
	addi	a4, a4, 48
	sb	a4, 2(a2)
	lb	a4, 0(s1)
	addi	a1, a1, 48
	sb	a1, 1(a2)
	lb	a1, -31(s1)
	addi	a4, a4, 48
	sb	a4, 0(a2)
	lb	a4, -30(s1)
	addi	a1, a1, 48
	sb	a1, 31(a2)
	lb	a1, -29(s1)
	addi	a4, a4, 48
	sb	a4, 30(a2)
	lb	a4, -28(s1)
	addi	a1, a1, 48
	sb	a1, 29(a2)
	lb	a1, -27(s1)
	addi	a4, a4, 48
	sb	a4, 28(a2)
	lb	a4, -26(s1)
	addi	a1, a1, 48
	sb	a1, 27(a2)
	lb	a1, -25(s1)
	addi	a4, a4, 48
	sb	a4, 26(a2)
	lb	a4, -24(s1)
	addi	a1, a1, 48
	sb	a1, 25(a2)
	lb	a1, -23(s1)
	addi	a4, a4, 48
	sb	a4, 24(a2)
	lb	a4, -22(s1)
	addi	a1, a1, 48
	sb	a1, 23(a2)
	lb	a1, -21(s1)
	addi	a4, a4, 48
	sb	a4, 22(a2)
	lb	a4, -20(s1)
	addi	a1, a1, 48
	sb	a1, 21(a2)
	lb	a1, -19(s1)
	addi	a4, a4, 48
	sb	a4, 20(a2)
	lb	a4, -18(s1)
	addi	a1, a1, 48
	sb	a1, 19(a2)
	lb	a1, -17(s1)
	lb	a5, -16(s1)
	addi	a4, a4, 48
	sb	a4, 18(a2)
	addi	a1, a1, 48
	sb	a1, 17(a2)
	addi	a1, a5, 48
	sb	a1, 16(a2)
	addi	s0, t6, 64
	sltu	a1, s0, t6
	addi	a2, t3, -2
	sltu	a4, a2, t3
	add	a4, t2, a4
	addi	t2, a4, -1
	or	a4, a2, t2
	add	t5, t5, a1
	mv	t6, s0
	mv	t3, a2
	bnez	a4, .LBB6_13
# %bb.14:                               # %.loopexit4
	beqz	a6, .LBB6_16
.LBB6_15:
	sub	a1, t1, s0
	addi	a2, sp, 10
	add	a1, a2, a1
	lb	a4, -15(a1)
	lb	a2, -14(a1)
	addi	a4, a4, 48
	or	a5, s0, s3
	add	a5, a0, a5
	sb	a4, 15(a5)
	lb	a4, -13(a1)
	addi	a2, a2, 48
	sb	a2, 14(a5)
	lb	a2, -12(a1)
	addi	a4, a4, 48
	sb	a4, 13(a5)
	lb	a4, -11(a1)
	addi	a2, a2, 48
	sb	a2, 12(a5)
	lb	a2, -10(a1)
	addi	a4, a4, 48
	sb	a4, 11(a5)
	lb	a4, -9(a1)
	addi	a2, a2, 48
	sb	a2, 10(a5)
	lb	a2, -8(a1)
	addi	a4, a4, 48
	sb	a4, 9(a5)
	lb	a4, -7(a1)
	addi	a2, a2, 48
	sb	a2, 8(a5)
	lb	a2, -6(a1)
	addi	a4, a4, 48
	sb	a4, 7(a5)
	lb	a4, -5(a1)
	addi	a2, a2, 48
	sb	a2, 6(a5)
	lb	a2, -4(a1)
	addi	a4, a4, 48
	sb	a4, 5(a5)
	lb	a4, -3(a1)
	addi	a2, a2, 48
	sb	a2, 4(a5)
	lb	a2, -2(a1)
	addi	a4, a4, 48
	sb	a4, 3(a5)
	lb	a4, -1(a1)
	addi	a2, a2, 48
	sb	a2, 2(a5)
	lb	a2, 0(a1)
	addi	a4, a4, 48
	sb	a4, 1(a5)
	lb	a4, -31(a1)
	addi	a2, a2, 48
	sb	a2, 0(a5)
	lb	a2, -30(a1)
	addi	a4, a4, 48
	sb	a4, 31(a5)
	lb	a4, -29(a1)
	addi	a2, a2, 48
	sb	a2, 30(a5)
	lb	a2, -28(a1)
	addi	a4, a4, 48
	sb	a4, 29(a5)
	lb	a4, -27(a1)
	addi	a2, a2, 48
	sb	a2, 28(a5)
	lb	a2, -26(a1)
	addi	a4, a4, 48
	sb	a4, 27(a5)
	lb	a4, -25(a1)
	addi	a2, a2, 48
	sb	a2, 26(a5)
	lb	a2, -24(a1)
	addi	a4, a4, 48
	sb	a4, 25(a5)
	lb	a4, -23(a1)
	addi	a2, a2, 48
	sb	a2, 24(a5)
	lb	a2, -22(a1)
	addi	a4, a4, 48
	sb	a4, 23(a5)
	lb	a4, -21(a1)
	addi	a2, a2, 48
	sb	a2, 22(a5)
	lb	a2, -20(a1)
	addi	a4, a4, 48
	sb	a4, 21(a5)
	lb	a4, -19(a1)
	addi	a2, a2, 48
	sb	a2, 20(a5)
	lb	a2, -18(a1)
	addi	a4, a4, 48
	sb	a4, 19(a5)
	lb	a4, -17(a1)
	lb	a1, -16(a1)
	addi	a2, a2, 48
	sb	a2, 18(a5)
	addi	a2, a4, 48
	sb	a2, 17(a5)
	addi	a1, a1, 48
	sb	a1, 16(a5)
.LBB6_16:
	xor	a2, s4, t0
	mv	a1, zero
	beqz	a2, .LBB6_19
.LBB6_17:                               # %.preheader10.preheader
	addi	a2, sp, 10
.LBB6_18:                               # %.preheader10
                                        # =>This Inner Loop Header: Depth=1
	sub	a4, t1, s4
	add	a4, a2, a4
	lb	a4, 0(a4)
	addi	a4, a4, 48
	add	a5, s4, s3
	add	a5, a0, a5
	addi	a3, s4, 1
	sltu	s1, a3, s4
	add	a1, a1, s1
	xor	s1, a3, t0
	or	s1, s1, a1
	sb	a4, 0(a5)
	mv	s4, a3
	bnez	s1, .LBB6_18
.LBB6_19:                               # %.loopexit
	add	a1, a0, s2
	sb	zero, 0(a1)
.LBB6_20:
	lw	s5, 20(sp)
	lw	s4, 24(sp)
	lw	s3, 28(sp)
	lw	s2, 32(sp)
	lw	s1, 36(sp)
	lw	s0, 40(sp)
	lw	ra, 44(sp)
	addi	sp, sp, 48
	ret
.LBB6_21:
	mv	s0, zero
	bnez	a6, .LBB6_15
	j	.LBB6_16
.Lfunc_end6:
	.size	toString, .Lfunc_end6-toString
                                        # -- End function
	.globl	__string_concatenate    # -- Begin function __string_concatenate
	.p2align	2
	.type	__string_concatenate,@function
__string_concatenate:                   # @__string_concatenate
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	sw	s0, 8(sp)
	sw	s1, 4(sp)
	sw	s2, 0(sp)
	mv	s2, a1
	mv	s1, a0
	call	strlen
	mv	s0, a0
	mv	a0, s2
	call	strlen
	add	a0, a0, s0
	addi	a0, a0, 1
	srai	a1, a0, 31
	call	malloc
	mv	s0, a0
	mv	a1, s1
	call	strcat
	mv	a0, s0
	mv	a1, s2
	lw	s2, 0(sp)
	lw	s1, 4(sp)
	lw	s0, 8(sp)
	lw	ra, 12(sp)
	addi	sp, sp, 16
	tail	strcat
.Lfunc_end7:
	.size	__string_concatenate, .Lfunc_end7-__string_concatenate
                                        # -- End function
	.globl	__string_equal          # -- Begin function __string_equal
	.p2align	2
	.type	__string_equal,@function
__string_equal:                         # @__string_equal
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	seqz	a0, a0
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end8:
	.size	__string_equal, .Lfunc_end8-__string_equal
                                        # -- End function
	.globl	__string_notEqual       # -- Begin function __string_notEqual
	.p2align	2
	.type	__string_notEqual,@function
__string_notEqual:                      # @__string_notEqual
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	snez	a0, a0
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end9:
	.size	__string_notEqual, .Lfunc_end9-__string_notEqual
                                        # -- End function
	.globl	__string_lessThan       # -- Begin function __string_lessThan
	.p2align	2
	.type	__string_lessThan,@function
__string_lessThan:                      # @__string_lessThan
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	srli	a0, a0, 31
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end10:
	.size	__string_lessThan, .Lfunc_end10-__string_lessThan
                                        # -- End function
	.globl	__string_greaterThan    # -- Begin function __string_greaterThan
	.p2align	2
	.type	__string_greaterThan,@function
__string_greaterThan:                   # @__string_greaterThan
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	sgtz	a0, a0
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end11:
	.size	__string_greaterThan, .Lfunc_end11-__string_greaterThan
                                        # -- End function
	.globl	__string_lessEqual      # -- Begin function __string_lessEqual
	.p2align	2
	.type	__string_lessEqual,@function
__string_lessEqual:                     # @__string_lessEqual
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	slti	a0, a0, 1
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end12:
	.size	__string_lessEqual, .Lfunc_end12-__string_lessEqual
                                        # -- End function
	.globl	__string_greaterEqual   # -- Begin function __string_greaterEqual
	.p2align	2
	.type	__string_greaterEqual,@function
__string_greaterEqual:                  # @__string_greaterEqual
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strcmp
	not	a0, a0
	srli	a0, a0, 31
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end13:
	.size	__string_greaterEqual, .Lfunc_end13-__string_greaterEqual
                                        # -- End function
	.globl	__string_length         # -- Begin function __string_length
	.p2align	2
	.type	__string_length,@function
__string_length:                        # @__string_length
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)
	call	strlen
	lw	ra, 12(sp)
	addi	sp, sp, 16
	ret
.Lfunc_end14:
	.size	__string_length, .Lfunc_end14-__string_length
                                        # -- End function
	.globl	__string_substring      # -- Begin function __string_substring
	.p2align	2
	.type	__string_substring,@function
__string_substring:                     # @__string_substring
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)
	sw	s0, 24(sp)
	sw	s1, 20(sp)
	sw	s2, 16(sp)
	sw	s3, 12(sp)
	mv	s3, a1
	mv	s2, a0
	sub	s0, a2, a1
	addi	a0, s0, 1
	srai	a1, a0, 31
	call	malloc
	addi	a1, zero, 1
	mv	s1, a0
	blt	s0, a1, .LBB15_2
# %bb.1:
	add	a1, s2, s3
	mv	a0, s1
	mv	a2, s0
	call	memcpy
.LBB15_2:
	add	a0, s1, s0
	sb	zero, 0(a0)
	mv	a0, s1
	lw	s3, 12(sp)
	lw	s2, 16(sp)
	lw	s1, 20(sp)
	lw	s0, 24(sp)
	lw	ra, 28(sp)
	addi	sp, sp, 32
	ret
.Lfunc_end15:
	.size	__string_substring, .Lfunc_end15-__string_substring
                                        # -- End function
	.globl	__string_parseInt       # -- Begin function __string_parseInt
	.p2align	2
	.type	__string_parseInt,@function
__string_parseInt:                      # @__string_parseInt
# %bb.0:
	lbu	a2, 0(a0)
	addi	a1, a2, -48
	andi	a1, a1, 255
	addi	a3, zero, 9
	bltu	a3, a1, .LBB16_4
# %bb.1:                                # %.preheader.preheader
	mv	a5, zero
	mv	a3, zero
	mv	a1, zero
	addi	a6, zero, 10
.LBB16_2:                               # %.preheader
                                        # =>This Inner Loop Header: Depth=1
	mul	a7, a1, a6
	addi	a4, a5, 1
	andi	a1, a2, 255
	add	a2, a0, a4
	lbu	a2, 0(a2)
	sltu	a5, a4, a5
	add	a3, a3, a5
	add	a1, a1, a7
	addi	a5, a2, -48
	andi	a7, a5, 255
	addi	a1, a1, -48
	mv	a5, a4
	bltu	a7, a6, .LBB16_2
# %bb.3:                                # %.loopexit
	mv	a0, a1
	ret
.LBB16_4:
	mv	a0, zero
	ret
.Lfunc_end16:
	.size	__string_parseInt, .Lfunc_end16-__string_parseInt
                                        # -- End function
	.globl	__string_ord            # -- Begin function __string_ord
	.p2align	2
	.type	__string_ord,@function
__string_ord:                           # @__string_ord
# %bb.0:
	add	a0, a0, a1
	lb	a0, 0(a0)
	ret
.Lfunc_end17:
	.size	__string_ord, .Lfunc_end17-__string_ord
                                        # -- End function
	.globl	__array_size            # -- Begin function __array_size
	.p2align	2
	.type	__array_size,@function
__array_size:                           # @__array_size
# %bb.0:
	lw	a0, -4(a0)
	ret
.Lfunc_end18:
	.size	__array_size, .Lfunc_end18-__array_size
                                        # -- End function
	.type	.L.str,@object          # @.str
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str:
	.asciz	"%s"
	.size	.L.str, 3

	.type	.L.str.2,@object        # @.str.2
.L.str.2:
	.asciz	"%d"
	.size	.L.str.2, 3

	.type	.L.str.3,@object        # @.str.3
.L.str.3:
	.asciz	"%d\n"
	.size	.L.str.3, 4

	.ident	"clang version 6.0.0-1ubuntu2 (tags/RELEASE_600/final)"
	.section	".note.GNU-stack","",@progbits
