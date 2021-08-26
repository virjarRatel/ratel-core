// Copyright (c) 2019, the R8 project authors. Please see the AUTHORS file
// for details. All rights reserved. Use of this source code is governed by a
// BSD-style license that can be found in the LICENSE file.

package com.android.tools.r8.ir.code;

public interface Opcodes {

  int ADD = 0;
  int ALWAYS_MATERIALIZING_DEFINITION = 1;
  int ALWAYS_MATERIALIZING_NOP = 2;
  int ALWAYS_MATERIALIZING_USER = 3;
  int AND = 4;
  int ARGUMENT = 5;
  int ARRAY_GET = 6;
  int ARRAY_LENGTH = 7;
  int ARRAY_PUT = 8;
  int ASSUME = 9;
  int CHECK_CAST = 10;
  int CMP = 11;
  int CONST_CLASS = 12;
  int CONST_METHOD_HANDLE = 13;
  int CONST_METHOD_TYPE = 14;
  int CONST_NUMBER = 15;
  int CONST_STRING = 16;
  int DEBUG_LOCAL_READ = 17;
  int DEBUG_LOCALS_CHANGE = 18;
  int DEBUG_POSITION = 19;
  int DEX_ITEM_BASED_CONST_STRING = 20;
  int DIV = 21;
  int DUP = 22;
  int DUP2 = 23;
  int GOTO = 24;
  int IF = 25;
  int INC = 26;
  int INSTANCE_GET = 27;
  int INSTANCE_OF = 28;
  int INSTANCE_PUT = 29;
  int INT_SWITCH = 30;
  int INVOKE_CUSTOM = 31;
  int INVOKE_DIRECT = 32;
  int INVOKE_INTERFACE = 33;
  int INVOKE_MULTI_NEW_ARRAY = 34;
  int INVOKE_NEW_ARRAY = 35;
  int INVOKE_POLYMORPHIC = 36;
  int INVOKE_STATIC = 37;
  int INVOKE_SUPER = 38;
  int INVOKE_VIRTUAL = 39;
  int LOAD = 40;
  int MONITOR = 41;
  int MOVE = 42;
  int MOVE_EXCEPTION = 43;
  int MUL = 44;
  int NEG = 45;
  int NEW_ARRAY_EMPTY = 46;
  int NEW_ARRAY_FILLED_DATA = 47;
  int NEW_INSTANCE = 48;
  int NOT = 49;
  int NUMBER_CONVERSION = 50;
  int OR = 51;
  int POP = 52;
  int REM = 53;
  int RETURN = 54;
  int SHL = 55;
  int SHR = 56;
  int STATIC_GET = 57;
  int STATIC_PUT = 58;
  int STORE = 59;
  int STRING_SWITCH = 60;
  int SUB = 61;
  int SWAP = 62;
  int THROW = 63;
  int USHR = 64;
  int XOR = 65;
}
