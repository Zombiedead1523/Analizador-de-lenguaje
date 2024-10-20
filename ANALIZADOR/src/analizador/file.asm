.MODEL SMALL
.CODE
Inicio:
mov Ax, @Data
mov Ds, Ax 
SaltoMien0:
mov Dx, offset opcion+2
mov Si, Dx
mov Cl, byte ptr [Si]
 
mov Dx, offset Var1
mov Si, Dx
mov Ch, byte ptr [Si] 

cmp Cl,Ch

je SaltoMien1
mov Ah, 09h
mov Dx, offset VAR2
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
mov Ah, 0Ah
mov Dx, offset n
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
xor Cx, Cx
mov Dx, offset n+2
xor Ax, Ax
mov Si, Dx
mov Al, byte ptr [Si] 
sub Al, 48
mov Dx, offset VAR3
xor bx, bx
mov Si, Dx
mov bl, byte ptr [Si] 
sub bl, 48
div bl
mov i, Ah
add i, 48
Salto1:
mov Dx, offset i
mov Si, Dx
mov Cl, byte ptr [Si] 
mov Dx, offset VAR4
mov Si, Dx
mov Ch, byte ptr [Si] 
cmp Cl, Ch
jne salto2
mov Ah, 09h
mov Dx, offset VAR5
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
mov Al, VAR6
sub Al, '0'
xor Cx, Cx
mov Si, offset n+2
mov Cl, byte ptr [Si]
sub Cl, '0'

sub Cl, Al
add Cl, 1

mov byte ptr [a], 1
add a, 48

SaltoFor0:

push Cx
xor Cx, Cx
mov Dx, offset a
mov Si, Dx
mov Al, byte ptr [Si] 
sub Al, 48
mov Dx, offset VAR7
mov Si, Dx
mov Ah, byte ptr [Si] 
sub Ah, 48
mul Ah
mov b, Al
add b, 48
mov Ah, 09h
mov Dx, offset b
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
inc VAR6
pop Cx
inc a
loop SaltoFor0

jmp Salto3
salto2:
jmp Salto3
Salto3:
Salto4:
mov Dx, offset i
mov Si, Dx
mov Cl, byte ptr [Si] 
mov Dx, offset VAR8
mov Si, Dx
mov Ch, byte ptr [Si] 
cmp Cl, Ch
jne salto5
mov Ah, 09h
mov Dx, offset VAR9
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
mov Al, VAR10
sub Al, '0'
xor Cx, Cx
mov Si, offset n+2
mov Cl, byte ptr [Si]
sub Cl, '0'

sub Cl, Al
add Cl, 1

mov byte ptr [a], 1
add a, 48

SaltoFor1:

push Cx
xor Cx, Cx
mov Dx, offset a
mov Si, Dx
mov Al, byte ptr [Si] 
sub Al, 48
mov Dx, offset VAR11
mov Si, Dx
mov Ah, byte ptr [Si] 
sub Ah, 48
mul Ah
mov b, Al
add b, 48
xor Cx, Cx
mov Dx, offset b
mov Si, Dx
mov Cl, byte ptr [Si] 
sub Cl, 48
mov Dx, offset VAR12
mov Si, Dx
mov Ch, byte ptr [Si] 
sub Ch, 48
xor b, 0
mov b, Cl
sub b, Ch
add b, 48
mov Ah, 09h
mov Dx, offset b
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
inc VAR10
pop Cx
inc a
loop SaltoFor1

jmp Salto6
salto5:
jmp Salto6
Salto6:
mov Ah, 09h
mov Dx, offset VAR13
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
mov Ah, 0Ah
mov Dx, offset opcion
int 21h
mov Ah, 09h
mov Dx, offset salto80
int 21h
jmp SaltoMien0

SaltoMien1:

mov AX, 4C00h
int 21h
.DATA
n db 255, ?, 255 dup("$")
i db 255, ?, 255 dup("$")
b db 255, ?, 255 dup("$")
a db 255, ?, 255 dup("$")
opcion db 255, ?, 255 dup("$")
salto80 db 10, 13, '$'
Var1 db '1','$'
VAR2 db 'Ingrese n$'
Var3 db '2','$'
VAR4 db '0','$'
VAR5 db 'es par$'
VAR6 db '1', '$'
Var7 db '2','$'
VAR8 db '1','$'
VAR9 db 'es impar$'
VAR10 db '1', '$'
Var11 db '2','$'
Var12 db '1','$'
VAR13 db 'quieres volver a correrlo?, ingresa valor distinto de 1$'
.STACK
END Inicio
