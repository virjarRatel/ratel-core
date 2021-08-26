// $ANTLR 3.5.2 smaliTreeWalker.g 2019-01-23 00:20:02

package org.jf.smali;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.antlr.runtime.BitSet;
import org.antlr.runtime.*;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.TreeNodeStream;
import org.antlr.runtime.tree.TreeParser;
import org.antlr.runtime.tree.TreeRuleReturnScope;
import org.jf.dexlib2.*;
import org.jf.dexlib2.builder.Label;
import org.jf.dexlib2.builder.MethodImplementationBuilder;
import org.jf.dexlib2.builder.SwitchLabelElement;
import org.jf.dexlib2.builder.instruction.*;
import org.jf.dexlib2.iface.Annotation;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.reference.FieldReference;
import org.jf.dexlib2.iface.reference.MethodReference;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.ImmutableAnnotation;
import org.jf.dexlib2.immutable.ImmutableAnnotationElement;
import org.jf.dexlib2.immutable.reference.ImmutableCallSiteReference;
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodHandleReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference;
import org.jf.dexlib2.immutable.reference.ImmutableMethodProtoReference;
import org.jf.dexlib2.immutable.reference.ImmutableReference;
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference;
import org.jf.dexlib2.immutable.value.*;
import org.jf.dexlib2.util.MethodUtil;
import org.jf.dexlib2.writer.InstructionFactory;
import org.jf.dexlib2.writer.builder.*;
import org.jf.util.LinearSearch;

import java.util.*;


import org.antlr.runtime.*;
import org.antlr.runtime.tree.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class smaliTreeWalker extends TreeParser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ACCESS_SPEC", "ANNOTATION_DIRECTIVE", 
		"ANNOTATION_VISIBILITY", "ARRAY_DATA_DIRECTIVE", "ARRAY_TYPE_PREFIX", 
		"ARROW", "AT", "BOOL_LITERAL", "BYTE_LITERAL", "CATCHALL_DIRECTIVE", "CATCH_DIRECTIVE", 
		"CHAR_LITERAL", "CLASS_DESCRIPTOR", "CLASS_DIRECTIVE", "CLOSE_BRACE", 
		"CLOSE_PAREN", "COLON", "COMMA", "DOTDOT", "DOUBLE_LITERAL", "DOUBLE_LITERAL_OR_ID", 
		"END_ANNOTATION_DIRECTIVE", "END_ARRAY_DATA_DIRECTIVE", "END_FIELD_DIRECTIVE", 
		"END_LOCAL_DIRECTIVE", "END_METHOD_DIRECTIVE", "END_PACKED_SWITCH_DIRECTIVE", 
		"END_PARAMETER_DIRECTIVE", "END_SPARSE_SWITCH_DIRECTIVE", "END_SUBANNOTATION_DIRECTIVE", 
		"ENUM_DIRECTIVE", "EPILOGUE_DIRECTIVE", "EQUAL", "FIELD_DIRECTIVE", "FIELD_OFFSET", 
		"FLOAT_LITERAL", "FLOAT_LITERAL_OR_ID", "IMPLEMENTS_DIRECTIVE", "INLINE_INDEX", 
		"INSTRUCTION_FORMAT10t", "INSTRUCTION_FORMAT10x", "INSTRUCTION_FORMAT10x_ODEX", 
		"INSTRUCTION_FORMAT11n", "INSTRUCTION_FORMAT11x", "INSTRUCTION_FORMAT12x", 
		"INSTRUCTION_FORMAT12x_OR_ID", "INSTRUCTION_FORMAT20bc", "INSTRUCTION_FORMAT20t", 
		"INSTRUCTION_FORMAT21c_FIELD", "INSTRUCTION_FORMAT21c_FIELD_ODEX", "INSTRUCTION_FORMAT21c_METHOD_HANDLE", 
		"INSTRUCTION_FORMAT21c_METHOD_TYPE", "INSTRUCTION_FORMAT21c_STRING", "INSTRUCTION_FORMAT21c_TYPE", 
		"INSTRUCTION_FORMAT21ih", "INSTRUCTION_FORMAT21lh", "INSTRUCTION_FORMAT21s", 
		"INSTRUCTION_FORMAT21t", "INSTRUCTION_FORMAT22b", "INSTRUCTION_FORMAT22c_FIELD", 
		"INSTRUCTION_FORMAT22c_FIELD_ODEX", "INSTRUCTION_FORMAT22c_TYPE", "INSTRUCTION_FORMAT22cs_FIELD", 
		"INSTRUCTION_FORMAT22s", "INSTRUCTION_FORMAT22s_OR_ID", "INSTRUCTION_FORMAT22t", 
		"INSTRUCTION_FORMAT22x", "INSTRUCTION_FORMAT23x", "INSTRUCTION_FORMAT30t", 
		"INSTRUCTION_FORMAT31c", "INSTRUCTION_FORMAT31i", "INSTRUCTION_FORMAT31i_OR_ID", 
		"INSTRUCTION_FORMAT31t", "INSTRUCTION_FORMAT32x", "INSTRUCTION_FORMAT35c_CALL_SITE", 
		"INSTRUCTION_FORMAT35c_METHOD", "INSTRUCTION_FORMAT35c_METHOD_ODEX", "INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE", 
		"INSTRUCTION_FORMAT35c_TYPE", "INSTRUCTION_FORMAT35mi_METHOD", "INSTRUCTION_FORMAT35ms_METHOD", 
		"INSTRUCTION_FORMAT3rc_CALL_SITE", "INSTRUCTION_FORMAT3rc_METHOD", "INSTRUCTION_FORMAT3rc_METHOD_ODEX", 
		"INSTRUCTION_FORMAT3rc_TYPE", "INSTRUCTION_FORMAT3rmi_METHOD", "INSTRUCTION_FORMAT3rms_METHOD", 
		"INSTRUCTION_FORMAT45cc_METHOD", "INSTRUCTION_FORMAT4rcc_METHOD", "INSTRUCTION_FORMAT51l", 
		"INTEGER_LITERAL", "INVALID_TOKEN", "I_ACCESS_LIST", "I_ANNOTATION", "I_ANNOTATIONS", 
		"I_ANNOTATION_ELEMENT", "I_ARRAY_ELEMENTS", "I_ARRAY_ELEMENT_SIZE", "I_CALL_SITE_EXTRA_ARGUMENTS", 
		"I_CALL_SITE_REFERENCE", "I_CATCH", "I_CATCHALL", "I_CATCHES", "I_CLASS_DEF", 
		"I_ENCODED_ARRAY", "I_ENCODED_ENUM", "I_ENCODED_FIELD", "I_ENCODED_METHOD", 
		"I_ENCODED_METHOD_HANDLE", "I_END_LOCAL", "I_EPILOGUE", "I_FIELD", "I_FIELDS", 
		"I_FIELD_INITIAL_VALUE", "I_FIELD_TYPE", "I_IMPLEMENTS", "I_LABEL", "I_LINE", 
		"I_LOCAL", "I_LOCALS", "I_METHOD", "I_METHODS", "I_METHOD_PROTOTYPE", 
		"I_METHOD_RETURN_TYPE", "I_ORDERED_METHOD_ITEMS", "I_PACKED_SWITCH_ELEMENTS", 
		"I_PACKED_SWITCH_START_KEY", "I_PARAMETER", "I_PARAMETERS", "I_PARAMETER_NOT_SPECIFIED", 
		"I_PROLOGUE", "I_REGISTERS", "I_REGISTER_LIST", "I_REGISTER_RANGE", "I_RESTART_LOCAL", 
		"I_SOURCE", "I_SPARSE_SWITCH_ELEMENTS", "I_STATEMENT_ARRAY_DATA", "I_STATEMENT_FORMAT10t", 
		"I_STATEMENT_FORMAT10x", "I_STATEMENT_FORMAT11n", "I_STATEMENT_FORMAT11x", 
		"I_STATEMENT_FORMAT12x", "I_STATEMENT_FORMAT20bc", "I_STATEMENT_FORMAT20t", 
		"I_STATEMENT_FORMAT21c_FIELD", "I_STATEMENT_FORMAT21c_METHOD_HANDLE", 
		"I_STATEMENT_FORMAT21c_METHOD_TYPE", "I_STATEMENT_FORMAT21c_STRING", "I_STATEMENT_FORMAT21c_TYPE", 
		"I_STATEMENT_FORMAT21ih", "I_STATEMENT_FORMAT21lh", "I_STATEMENT_FORMAT21s", 
		"I_STATEMENT_FORMAT21t", "I_STATEMENT_FORMAT22b", "I_STATEMENT_FORMAT22c_FIELD", 
		"I_STATEMENT_FORMAT22c_TYPE", "I_STATEMENT_FORMAT22s", "I_STATEMENT_FORMAT22t", 
		"I_STATEMENT_FORMAT22x", "I_STATEMENT_FORMAT23x", "I_STATEMENT_FORMAT30t", 
		"I_STATEMENT_FORMAT31c", "I_STATEMENT_FORMAT31i", "I_STATEMENT_FORMAT31t", 
		"I_STATEMENT_FORMAT32x", "I_STATEMENT_FORMAT35c_CALL_SITE", "I_STATEMENT_FORMAT35c_METHOD", 
		"I_STATEMENT_FORMAT35c_TYPE", "I_STATEMENT_FORMAT3rc_CALL_SITE", "I_STATEMENT_FORMAT3rc_METHOD", 
		"I_STATEMENT_FORMAT3rc_TYPE", "I_STATEMENT_FORMAT45cc_METHOD", "I_STATEMENT_FORMAT4rcc_METHOD", 
		"I_STATEMENT_FORMAT51l", "I_STATEMENT_PACKED_SWITCH", "I_STATEMENT_SPARSE_SWITCH", 
		"I_SUBANNOTATION", "I_SUPER", "LINE_COMMENT", "LINE_DIRECTIVE", "LOCALS_DIRECTIVE", 
		"LOCAL_DIRECTIVE", "LONG_LITERAL", "MEMBER_NAME", "METHOD_DIRECTIVE", 
		"METHOD_HANDLE_TYPE_FIELD", "METHOD_HANDLE_TYPE_METHOD", "NEGATIVE_INTEGER_LITERAL", 
		"NULL_LITERAL", "OPEN_BRACE", "OPEN_PAREN", "PACKED_SWITCH_DIRECTIVE", 
		"PARAMETER_DIRECTIVE", "PARAM_LIST_OR_ID_PRIMITIVE_TYPE", "POSITIVE_INTEGER_LITERAL", 
		"PRIMITIVE_TYPE", "PROLOGUE_DIRECTIVE", "REGISTER", "REGISTERS_DIRECTIVE", 
		"RESTART_LOCAL_DIRECTIVE", "SHORT_LITERAL", "SIMPLE_NAME", "SOURCE_DIRECTIVE", 
		"SPARSE_SWITCH_DIRECTIVE", "STRING_LITERAL", "SUBANNOTATION_DIRECTIVE", 
		"SUPER_DIRECTIVE", "VERIFICATION_ERROR_TYPE", "VOID_TYPE", "VTABLE_INDEX", 
		"WHITE_SPACE"
	};
	public static final int EOF=-1;
	public static final int ACCESS_SPEC=4;
	public static final int ANNOTATION_DIRECTIVE=5;
	public static final int ANNOTATION_VISIBILITY=6;
	public static final int ARRAY_DATA_DIRECTIVE=7;
	public static final int ARRAY_TYPE_PREFIX=8;
	public static final int ARROW=9;
	public static final int AT=10;
	public static final int BOOL_LITERAL=11;
	public static final int BYTE_LITERAL=12;
	public static final int CATCHALL_DIRECTIVE=13;
	public static final int CATCH_DIRECTIVE=14;
	public static final int CHAR_LITERAL=15;
	public static final int CLASS_DESCRIPTOR=16;
	public static final int CLASS_DIRECTIVE=17;
	public static final int CLOSE_BRACE=18;
	public static final int CLOSE_PAREN=19;
	public static final int COLON=20;
	public static final int COMMA=21;
	public static final int DOTDOT=22;
	public static final int DOUBLE_LITERAL=23;
	public static final int DOUBLE_LITERAL_OR_ID=24;
	public static final int END_ANNOTATION_DIRECTIVE=25;
	public static final int END_ARRAY_DATA_DIRECTIVE=26;
	public static final int END_FIELD_DIRECTIVE=27;
	public static final int END_LOCAL_DIRECTIVE=28;
	public static final int END_METHOD_DIRECTIVE=29;
	public static final int END_PACKED_SWITCH_DIRECTIVE=30;
	public static final int END_PARAMETER_DIRECTIVE=31;
	public static final int END_SPARSE_SWITCH_DIRECTIVE=32;
	public static final int END_SUBANNOTATION_DIRECTIVE=33;
	public static final int ENUM_DIRECTIVE=34;
	public static final int EPILOGUE_DIRECTIVE=35;
	public static final int EQUAL=36;
	public static final int FIELD_DIRECTIVE=37;
	public static final int FIELD_OFFSET=38;
	public static final int FLOAT_LITERAL=39;
	public static final int FLOAT_LITERAL_OR_ID=40;
	public static final int IMPLEMENTS_DIRECTIVE=41;
	public static final int INLINE_INDEX=42;
	public static final int INSTRUCTION_FORMAT10t=43;
	public static final int INSTRUCTION_FORMAT10x=44;
	public static final int INSTRUCTION_FORMAT10x_ODEX=45;
	public static final int INSTRUCTION_FORMAT11n=46;
	public static final int INSTRUCTION_FORMAT11x=47;
	public static final int INSTRUCTION_FORMAT12x=48;
	public static final int INSTRUCTION_FORMAT12x_OR_ID=49;
	public static final int INSTRUCTION_FORMAT20bc=50;
	public static final int INSTRUCTION_FORMAT20t=51;
	public static final int INSTRUCTION_FORMAT21c_FIELD=52;
	public static final int INSTRUCTION_FORMAT21c_FIELD_ODEX=53;
	public static final int INSTRUCTION_FORMAT21c_METHOD_HANDLE=54;
	public static final int INSTRUCTION_FORMAT21c_METHOD_TYPE=55;
	public static final int INSTRUCTION_FORMAT21c_STRING=56;
	public static final int INSTRUCTION_FORMAT21c_TYPE=57;
	public static final int INSTRUCTION_FORMAT21ih=58;
	public static final int INSTRUCTION_FORMAT21lh=59;
	public static final int INSTRUCTION_FORMAT21s=60;
	public static final int INSTRUCTION_FORMAT21t=61;
	public static final int INSTRUCTION_FORMAT22b=62;
	public static final int INSTRUCTION_FORMAT22c_FIELD=63;
	public static final int INSTRUCTION_FORMAT22c_FIELD_ODEX=64;
	public static final int INSTRUCTION_FORMAT22c_TYPE=65;
	public static final int INSTRUCTION_FORMAT22cs_FIELD=66;
	public static final int INSTRUCTION_FORMAT22s=67;
	public static final int INSTRUCTION_FORMAT22s_OR_ID=68;
	public static final int INSTRUCTION_FORMAT22t=69;
	public static final int INSTRUCTION_FORMAT22x=70;
	public static final int INSTRUCTION_FORMAT23x=71;
	public static final int INSTRUCTION_FORMAT30t=72;
	public static final int INSTRUCTION_FORMAT31c=73;
	public static final int INSTRUCTION_FORMAT31i=74;
	public static final int INSTRUCTION_FORMAT31i_OR_ID=75;
	public static final int INSTRUCTION_FORMAT31t=76;
	public static final int INSTRUCTION_FORMAT32x=77;
	public static final int INSTRUCTION_FORMAT35c_CALL_SITE=78;
	public static final int INSTRUCTION_FORMAT35c_METHOD=79;
	public static final int INSTRUCTION_FORMAT35c_METHOD_ODEX=80;
	public static final int INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE=81;
	public static final int INSTRUCTION_FORMAT35c_TYPE=82;
	public static final int INSTRUCTION_FORMAT35mi_METHOD=83;
	public static final int INSTRUCTION_FORMAT35ms_METHOD=84;
	public static final int INSTRUCTION_FORMAT3rc_CALL_SITE=85;
	public static final int INSTRUCTION_FORMAT3rc_METHOD=86;
	public static final int INSTRUCTION_FORMAT3rc_METHOD_ODEX=87;
	public static final int INSTRUCTION_FORMAT3rc_TYPE=88;
	public static final int INSTRUCTION_FORMAT3rmi_METHOD=89;
	public static final int INSTRUCTION_FORMAT3rms_METHOD=90;
	public static final int INSTRUCTION_FORMAT45cc_METHOD=91;
	public static final int INSTRUCTION_FORMAT4rcc_METHOD=92;
	public static final int INSTRUCTION_FORMAT51l=93;
	public static final int INTEGER_LITERAL=94;
	public static final int INVALID_TOKEN=95;
	public static final int I_ACCESS_LIST=96;
	public static final int I_ANNOTATION=97;
	public static final int I_ANNOTATIONS=98;
	public static final int I_ANNOTATION_ELEMENT=99;
	public static final int I_ARRAY_ELEMENTS=100;
	public static final int I_ARRAY_ELEMENT_SIZE=101;
	public static final int I_CALL_SITE_EXTRA_ARGUMENTS=102;
	public static final int I_CALL_SITE_REFERENCE=103;
	public static final int I_CATCH=104;
	public static final int I_CATCHALL=105;
	public static final int I_CATCHES=106;
	public static final int I_CLASS_DEF=107;
	public static final int I_ENCODED_ARRAY=108;
	public static final int I_ENCODED_ENUM=109;
	public static final int I_ENCODED_FIELD=110;
	public static final int I_ENCODED_METHOD=111;
	public static final int I_ENCODED_METHOD_HANDLE=112;
	public static final int I_END_LOCAL=113;
	public static final int I_EPILOGUE=114;
	public static final int I_FIELD=115;
	public static final int I_FIELDS=116;
	public static final int I_FIELD_INITIAL_VALUE=117;
	public static final int I_FIELD_TYPE=118;
	public static final int I_IMPLEMENTS=119;
	public static final int I_LABEL=120;
	public static final int I_LINE=121;
	public static final int I_LOCAL=122;
	public static final int I_LOCALS=123;
	public static final int I_METHOD=124;
	public static final int I_METHODS=125;
	public static final int I_METHOD_PROTOTYPE=126;
	public static final int I_METHOD_RETURN_TYPE=127;
	public static final int I_ORDERED_METHOD_ITEMS=128;
	public static final int I_PACKED_SWITCH_ELEMENTS=129;
	public static final int I_PACKED_SWITCH_START_KEY=130;
	public static final int I_PARAMETER=131;
	public static final int I_PARAMETERS=132;
	public static final int I_PARAMETER_NOT_SPECIFIED=133;
	public static final int I_PROLOGUE=134;
	public static final int I_REGISTERS=135;
	public static final int I_REGISTER_LIST=136;
	public static final int I_REGISTER_RANGE=137;
	public static final int I_RESTART_LOCAL=138;
	public static final int I_SOURCE=139;
	public static final int I_SPARSE_SWITCH_ELEMENTS=140;
	public static final int I_STATEMENT_ARRAY_DATA=141;
	public static final int I_STATEMENT_FORMAT10t=142;
	public static final int I_STATEMENT_FORMAT10x=143;
	public static final int I_STATEMENT_FORMAT11n=144;
	public static final int I_STATEMENT_FORMAT11x=145;
	public static final int I_STATEMENT_FORMAT12x=146;
	public static final int I_STATEMENT_FORMAT20bc=147;
	public static final int I_STATEMENT_FORMAT20t=148;
	public static final int I_STATEMENT_FORMAT21c_FIELD=149;
	public static final int I_STATEMENT_FORMAT21c_METHOD_HANDLE=150;
	public static final int I_STATEMENT_FORMAT21c_METHOD_TYPE=151;
	public static final int I_STATEMENT_FORMAT21c_STRING=152;
	public static final int I_STATEMENT_FORMAT21c_TYPE=153;
	public static final int I_STATEMENT_FORMAT21ih=154;
	public static final int I_STATEMENT_FORMAT21lh=155;
	public static final int I_STATEMENT_FORMAT21s=156;
	public static final int I_STATEMENT_FORMAT21t=157;
	public static final int I_STATEMENT_FORMAT22b=158;
	public static final int I_STATEMENT_FORMAT22c_FIELD=159;
	public static final int I_STATEMENT_FORMAT22c_TYPE=160;
	public static final int I_STATEMENT_FORMAT22s=161;
	public static final int I_STATEMENT_FORMAT22t=162;
	public static final int I_STATEMENT_FORMAT22x=163;
	public static final int I_STATEMENT_FORMAT23x=164;
	public static final int I_STATEMENT_FORMAT30t=165;
	public static final int I_STATEMENT_FORMAT31c=166;
	public static final int I_STATEMENT_FORMAT31i=167;
	public static final int I_STATEMENT_FORMAT31t=168;
	public static final int I_STATEMENT_FORMAT32x=169;
	public static final int I_STATEMENT_FORMAT35c_CALL_SITE=170;
	public static final int I_STATEMENT_FORMAT35c_METHOD=171;
	public static final int I_STATEMENT_FORMAT35c_TYPE=172;
	public static final int I_STATEMENT_FORMAT3rc_CALL_SITE=173;
	public static final int I_STATEMENT_FORMAT3rc_METHOD=174;
	public static final int I_STATEMENT_FORMAT3rc_TYPE=175;
	public static final int I_STATEMENT_FORMAT45cc_METHOD=176;
	public static final int I_STATEMENT_FORMAT4rcc_METHOD=177;
	public static final int I_STATEMENT_FORMAT51l=178;
	public static final int I_STATEMENT_PACKED_SWITCH=179;
	public static final int I_STATEMENT_SPARSE_SWITCH=180;
	public static final int I_SUBANNOTATION=181;
	public static final int I_SUPER=182;
	public static final int LINE_COMMENT=183;
	public static final int LINE_DIRECTIVE=184;
	public static final int LOCALS_DIRECTIVE=185;
	public static final int LOCAL_DIRECTIVE=186;
	public static final int LONG_LITERAL=187;
	public static final int MEMBER_NAME=188;
	public static final int METHOD_DIRECTIVE=189;
	public static final int METHOD_HANDLE_TYPE_FIELD=190;
	public static final int METHOD_HANDLE_TYPE_METHOD=191;
	public static final int NEGATIVE_INTEGER_LITERAL=192;
	public static final int NULL_LITERAL=193;
	public static final int OPEN_BRACE=194;
	public static final int OPEN_PAREN=195;
	public static final int PACKED_SWITCH_DIRECTIVE=196;
	public static final int PARAMETER_DIRECTIVE=197;
	public static final int PARAM_LIST_OR_ID_PRIMITIVE_TYPE=198;
	public static final int POSITIVE_INTEGER_LITERAL=199;
	public static final int PRIMITIVE_TYPE=200;
	public static final int PROLOGUE_DIRECTIVE=201;
	public static final int REGISTER=202;
	public static final int REGISTERS_DIRECTIVE=203;
	public static final int RESTART_LOCAL_DIRECTIVE=204;
	public static final int SHORT_LITERAL=205;
	public static final int SIMPLE_NAME=206;
	public static final int SOURCE_DIRECTIVE=207;
	public static final int SPARSE_SWITCH_DIRECTIVE=208;
	public static final int STRING_LITERAL=209;
	public static final int SUBANNOTATION_DIRECTIVE=210;
	public static final int SUPER_DIRECTIVE=211;
	public static final int VERIFICATION_ERROR_TYPE=212;
	public static final int VOID_TYPE=213;
	public static final int VTABLE_INDEX=214;
	public static final int WHITE_SPACE=215;

	// delegates
	public TreeParser[] getDelegates() {
		return new TreeParser[] {};
	}

	// delegators


	public smaliTreeWalker(TreeNodeStream input) {
		this(input, new RecognizerSharedState());
	}
	public smaliTreeWalker(TreeNodeStream input, RecognizerSharedState state) {
		super(input, state);
	}

	@Override public String[] getTokenNames() { return smaliTreeWalker.tokenNames; }
	@Override public String getGrammarFileName() { return "smaliTreeWalker.g"; }


	  public String classType;
	  private boolean verboseErrors = false;
	  private int apiLevel = 15;
	  private Opcodes opcodes = Opcodes.forApi(apiLevel);
	  private DexBuilder dexBuilder;
	  private int callSiteNameIndex = 0;

	  public void setDexBuilder(DexBuilder dexBuilder) {
	      this.dexBuilder = dexBuilder;
	  }

	  public void setApiLevel(int apiLevel) {
	      this.opcodes = Opcodes.forApi(apiLevel);
	      this.apiLevel = apiLevel;
	  }

	  public void setVerboseErrors(boolean verboseErrors) {
	    this.verboseErrors = verboseErrors;
	  }

	  private byte parseRegister_nibble(String register)
	      throws SemanticException {
	    int totalMethodRegisters = method_stack.peek().totalMethodRegisters;
	    int methodParameterRegisters = method_stack.peek().methodParameterRegisters;

	    //register should be in the format "v12"
	    int val = Byte.parseByte(register.substring(1));
	    if (register.charAt(0) == 'p') {
	      val = totalMethodRegisters - methodParameterRegisters + val;
	    }
	    if (val >= 2<<4) {
	      throw new SemanticException(input, "The maximum allowed register in this context is list of registers is v15");
	    }
	    //the parser wouldn't have accepted a negative register, i.e. v-1, so we don't have to check for val<0;
	    return (byte)val;
	  }

	  //return a short, because java's byte is signed
	  private short parseRegister_byte(String register)
	      throws SemanticException {
	    int totalMethodRegisters = method_stack.peek().totalMethodRegisters;
	    int methodParameterRegisters = method_stack.peek().methodParameterRegisters;
	    //register should be in the format "v123"
	    int val = Short.parseShort(register.substring(1));
	    if (register.charAt(0) == 'p') {
	      val = totalMethodRegisters - methodParameterRegisters + val;
	    }
	    if (val >= 2<<8) {
	      throw new SemanticException(input, "The maximum allowed register in this context is v255");
	    }
	    return (short)val;
	  }

	  //return an int because java's short is signed
	  private int parseRegister_short(String register)
	      throws SemanticException {
	    int totalMethodRegisters = method_stack.peek().totalMethodRegisters;
	    int methodParameterRegisters = method_stack.peek().methodParameterRegisters;
	    //register should be in the format "v12345"
	    int val = Integer.parseInt(register.substring(1));
	    if (register.charAt(0) == 'p') {
	      val = totalMethodRegisters - methodParameterRegisters + val;
	    }
	    if (val >= 2<<16) {
	      throw new SemanticException(input, "The maximum allowed register in this context is v65535");
	    }
	    //the parser wouldn't accept a negative register, i.e. v-1, so we don't have to check for val<0;
	    return val;
	  }

	  public String getErrorMessage(RecognitionException e, String[] tokenNames) {
	    if ( e instanceof SemanticException ) {
	      return e.getMessage();
	    } else {
	      return super.getErrorMessage(e, tokenNames);
	    }
	  }

	  public String getErrorHeader(RecognitionException e) {
	    return getSourceName()+"["+ e.line+","+e.charPositionInLine+"]";
	  }



	// $ANTLR start "smali_file"
	// smaliTreeWalker.g:163:1: smali_file returns [ClassDef classDef] : ^( I_CLASS_DEF header methods fields annotations ) ;
	public final ClassDef smali_file() throws RecognitionException {
		ClassDef classDef = null;


		TreeRuleReturnScope header1 =null;
		Set<Annotation> annotations2 =null;
		List<BuilderField> fields3 =null;
		List<BuilderMethod> methods4 =null;

		try {
			// smaliTreeWalker.g:164:3: ( ^( I_CLASS_DEF header methods fields annotations ) )
			// smaliTreeWalker.g:164:5: ^( I_CLASS_DEF header methods fields annotations )
			{
			match(input,I_CLASS_DEF,FOLLOW_I_CLASS_DEF_in_smali_file52); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_header_in_smali_file54);
			header1=header();
			state._fsp--;

			pushFollow(FOLLOW_methods_in_smali_file56);
			methods4=methods();
			state._fsp--;

			pushFollow(FOLLOW_fields_in_smali_file58);
			fields3=fields();
			state._fsp--;

			pushFollow(FOLLOW_annotations_in_smali_file60);
			annotations2=annotations();
			state._fsp--;

			match(input, Token.UP, null); 


			    classDef = dexBuilder.internClassDef((header1!=null?((smaliTreeWalker.header_return)header1).classType:null), (header1!=null?((smaliTreeWalker.header_return)header1).accessFlags:0), (header1!=null?((smaliTreeWalker.header_return)header1).superType:null),
			            (header1!=null?((smaliTreeWalker.header_return)header1).implementsList:null), (header1!=null?((smaliTreeWalker.header_return)header1).sourceSpec:null), annotations2, fields3, methods4);
			  
			}

		}
		catch (Exception ex) {

			    if (verboseErrors) {
			      ex.printStackTrace(System.err);
			    }
			    reportError(new SemanticException(input, ex));
			  
		}

		finally {
			// do for sure before leaving
		}
		return classDef;
	}
	// $ANTLR end "smali_file"


	public static class header_return extends TreeRuleReturnScope {
		public String classType;
		public int accessFlags;
		public String superType;
		public List<String> implementsList;
		public String sourceSpec;
	};


	// $ANTLR start "header"
	// smaliTreeWalker.g:177:1: header returns [String classType, int accessFlags, String superType, List<String> implementsList, String sourceSpec] : class_spec ( super_spec )? implements_list source_spec ;
	public final smaliTreeWalker.header_return header() throws RecognitionException {
		smaliTreeWalker.header_return retval = new smaliTreeWalker.header_return();
		retval.start = input.LT(1);

		TreeRuleReturnScope class_spec5 =null;
		String super_spec6 =null;
		List<String> implements_list7 =null;
		String source_spec8 =null;

		try {
			// smaliTreeWalker.g:178:3: ( class_spec ( super_spec )? implements_list source_spec )
			// smaliTreeWalker.g:178:3: class_spec ( super_spec )? implements_list source_spec
			{
			pushFollow(FOLLOW_class_spec_in_header85);
			class_spec5=class_spec();
			state._fsp--;

			// smaliTreeWalker.g:178:14: ( super_spec )?
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0==I_SUPER) ) {
				alt1=1;
			}
			switch (alt1) {
				case 1 :
					// smaliTreeWalker.g:178:14: super_spec
					{
					pushFollow(FOLLOW_super_spec_in_header87);
					super_spec6=super_spec();
					state._fsp--;

					}
					break;

			}

			pushFollow(FOLLOW_implements_list_in_header90);
			implements_list7=implements_list();
			state._fsp--;

			pushFollow(FOLLOW_source_spec_in_header92);
			source_spec8=source_spec();
			state._fsp--;


			    classType = (class_spec5!=null?((smaliTreeWalker.class_spec_return)class_spec5).type:null);
			    retval.classType = classType;
			    retval.accessFlags = (class_spec5!=null?((smaliTreeWalker.class_spec_return)class_spec5).accessFlags:0);
			    retval.superType = super_spec6;
			    retval.implementsList = implements_list7;
			    retval.sourceSpec = source_spec8;
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "header"


	public static class class_spec_return extends TreeRuleReturnScope {
		public String type;
		public int accessFlags;
	};


	// $ANTLR start "class_spec"
	// smaliTreeWalker.g:189:1: class_spec returns [String type, int accessFlags] : CLASS_DESCRIPTOR access_list ;
	public final smaliTreeWalker.class_spec_return class_spec() throws RecognitionException {
		smaliTreeWalker.class_spec_return retval = new smaliTreeWalker.class_spec_return();
		retval.start = input.LT(1);

		CommonTree CLASS_DESCRIPTOR9=null;
		int access_list10 =0;

		try {
			// smaliTreeWalker.g:190:3: ( CLASS_DESCRIPTOR access_list )
			// smaliTreeWalker.g:190:5: CLASS_DESCRIPTOR access_list
			{
			CLASS_DESCRIPTOR9=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_class_spec110); 
			pushFollow(FOLLOW_access_list_in_class_spec112);
			access_list10=access_list();
			state._fsp--;


			    retval.type = (CLASS_DESCRIPTOR9!=null?CLASS_DESCRIPTOR9.getText():null);
			    retval.accessFlags = access_list10;
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "class_spec"



	// $ANTLR start "super_spec"
	// smaliTreeWalker.g:196:1: super_spec returns [String type] : ^( I_SUPER CLASS_DESCRIPTOR ) ;
	public final String super_spec() throws RecognitionException {
		String type = null;


		CommonTree CLASS_DESCRIPTOR11=null;

		try {
			// smaliTreeWalker.g:197:3: ( ^( I_SUPER CLASS_DESCRIPTOR ) )
			// smaliTreeWalker.g:197:5: ^( I_SUPER CLASS_DESCRIPTOR )
			{
			match(input,I_SUPER,FOLLOW_I_SUPER_in_super_spec130); 
			match(input, Token.DOWN, null); 
			CLASS_DESCRIPTOR11=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_super_spec132); 
			match(input, Token.UP, null); 


			    type = (CLASS_DESCRIPTOR11!=null?CLASS_DESCRIPTOR11.getText():null);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return type;
	}
	// $ANTLR end "super_spec"



	// $ANTLR start "implements_spec"
	// smaliTreeWalker.g:203:1: implements_spec returns [String type] : ^( I_IMPLEMENTS CLASS_DESCRIPTOR ) ;
	public final String implements_spec() throws RecognitionException {
		String type = null;


		CommonTree CLASS_DESCRIPTOR12=null;

		try {
			// smaliTreeWalker.g:204:3: ( ^( I_IMPLEMENTS CLASS_DESCRIPTOR ) )
			// smaliTreeWalker.g:204:5: ^( I_IMPLEMENTS CLASS_DESCRIPTOR )
			{
			match(input,I_IMPLEMENTS,FOLLOW_I_IMPLEMENTS_in_implements_spec152); 
			match(input, Token.DOWN, null); 
			CLASS_DESCRIPTOR12=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_implements_spec154); 
			match(input, Token.UP, null); 


			    type = (CLASS_DESCRIPTOR12!=null?CLASS_DESCRIPTOR12.getText():null);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return type;
	}
	// $ANTLR end "implements_spec"



	// $ANTLR start "implements_list"
	// smaliTreeWalker.g:209:1: implements_list returns [List<String> implementsList] : ( implements_spec )* ;
	public final List<String> implements_list() throws RecognitionException {
		List<String> implementsList = null;


		String implements_spec13 =null;

		 List<String> typeList; 
		try {
			// smaliTreeWalker.g:211:3: ( ( implements_spec )* )
			// smaliTreeWalker.g:211:5: ( implements_spec )*
			{
			typeList = Lists.newArrayList();
			// smaliTreeWalker.g:212:5: ( implements_spec )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==I_IMPLEMENTS) ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// smaliTreeWalker.g:212:6: implements_spec
					{
					pushFollow(FOLLOW_implements_spec_in_implements_list184);
					implements_spec13=implements_spec();
					state._fsp--;

					typeList.add(implements_spec13);
					}
					break;

				default :
					break loop2;
				}
			}


			    if (typeList.size() > 0) {
			      implementsList = typeList;
			    } else {
			      implementsList = null;
			    }
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return implementsList;
	}
	// $ANTLR end "implements_list"



	// $ANTLR start "source_spec"
	// smaliTreeWalker.g:221:1: source_spec returns [String source] : ( ^( I_SOURCE string_literal ) |);
	public final String source_spec() throws RecognitionException {
		String source = null;


		String string_literal14 =null;

		try {
			// smaliTreeWalker.g:222:3: ( ^( I_SOURCE string_literal ) |)
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==I_SOURCE) ) {
				alt3=1;
			}
			else if ( (LA3_0==I_METHODS) ) {
				alt3=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 3, 0, input);
				throw nvae;
			}

			switch (alt3) {
				case 1 :
					// smaliTreeWalker.g:222:5: ^( I_SOURCE string_literal )
					{
					source = null;
					match(input,I_SOURCE,FOLLOW_I_SOURCE_in_source_spec213); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_string_literal_in_source_spec215);
					string_literal14=string_literal();
					state._fsp--;

					source = string_literal14;
					match(input, Token.UP, null); 

					}
					break;
				case 2 :
					// smaliTreeWalker.g:224:16: 
					{
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return source;
	}
	// $ANTLR end "source_spec"



	// $ANTLR start "access_list"
	// smaliTreeWalker.g:226:1: access_list returns [int value] : ^( I_ACCESS_LIST ( ACCESS_SPEC )* ) ;
	public final int access_list() throws RecognitionException {
		int value = 0;


		CommonTree ACCESS_SPEC15=null;


		    value = 0;
		  
		try {
			// smaliTreeWalker.g:231:3: ( ^( I_ACCESS_LIST ( ACCESS_SPEC )* ) )
			// smaliTreeWalker.g:231:5: ^( I_ACCESS_LIST ( ACCESS_SPEC )* )
			{
			match(input,I_ACCESS_LIST,FOLLOW_I_ACCESS_LIST_in_access_list248); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:232:7: ( ACCESS_SPEC )*
				loop4:
				while (true) {
					int alt4=2;
					int LA4_0 = input.LA(1);
					if ( (LA4_0==ACCESS_SPEC) ) {
						alt4=1;
					}

					switch (alt4) {
					case 1 :
						// smaliTreeWalker.g:233:9: ACCESS_SPEC
						{
						ACCESS_SPEC15=(CommonTree)match(input,ACCESS_SPEC,FOLLOW_ACCESS_SPEC_in_access_list266); 

						          value |= AccessFlags.getAccessFlag(ACCESS_SPEC15.getText()).getValue();
						        
						}
						break;

					default :
						break loop4;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "access_list"



	// $ANTLR start "fields"
	// smaliTreeWalker.g:240:1: fields returns [List<BuilderField> fields] : ^( I_FIELDS ( field )* ) ;
	public final List<BuilderField> fields() throws RecognitionException {
		List<BuilderField> fields = null;


		BuilderField field16 =null;

		fields = Lists.newArrayList();
		try {
			// smaliTreeWalker.g:242:3: ( ^( I_FIELDS ( field )* ) )
			// smaliTreeWalker.g:242:5: ^( I_FIELDS ( field )* )
			{
			match(input,I_FIELDS,FOLLOW_I_FIELDS_in_fields308); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:243:7: ( field )*
				loop5:
				while (true) {
					int alt5=2;
					int LA5_0 = input.LA(1);
					if ( (LA5_0==I_FIELD) ) {
						alt5=1;
					}

					switch (alt5) {
					case 1 :
						// smaliTreeWalker.g:243:8: field
						{
						pushFollow(FOLLOW_field_in_fields317);
						field16=field();
						state._fsp--;


						        fields.add(field16);
						      
						}
						break;

					default :
						break loop5;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return fields;
	}
	// $ANTLR end "fields"



	// $ANTLR start "methods"
	// smaliTreeWalker.g:248:1: methods returns [List<BuilderMethod> methods] : ^( I_METHODS ( method )* ) ;
	public final List<BuilderMethod> methods() throws RecognitionException {
		List<BuilderMethod> methods = null;


		BuilderMethod method17 =null;

		methods = Lists.newArrayList();
		try {
			// smaliTreeWalker.g:250:3: ( ^( I_METHODS ( method )* ) )
			// smaliTreeWalker.g:250:5: ^( I_METHODS ( method )* )
			{
			match(input,I_METHODS,FOLLOW_I_METHODS_in_methods349); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:251:7: ( method )*
				loop6:
				while (true) {
					int alt6=2;
					int LA6_0 = input.LA(1);
					if ( (LA6_0==I_METHOD) ) {
						alt6=1;
					}

					switch (alt6) {
					case 1 :
						// smaliTreeWalker.g:251:8: method
						{
						pushFollow(FOLLOW_method_in_methods358);
						method17=method();
						state._fsp--;


						        methods.add(method17);
						      
						}
						break;

					default :
						break loop6;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return methods;
	}
	// $ANTLR end "methods"



	// $ANTLR start "field"
	// smaliTreeWalker.g:256:1: field returns [BuilderField field] : ^( I_FIELD SIMPLE_NAME access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) field_initial_value ( annotations )? ) ;
	public final BuilderField field() throws RecognitionException {
		BuilderField field = null;


		CommonTree SIMPLE_NAME20=null;
		int access_list18 =0;
		EncodedValue field_initial_value19 =null;
		TreeRuleReturnScope nonvoid_type_descriptor21 =null;
		Set<Annotation> annotations22 =null;

		try {
			// smaliTreeWalker.g:257:3: ( ^( I_FIELD SIMPLE_NAME access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) field_initial_value ( annotations )? ) )
			// smaliTreeWalker.g:257:4: ^( I_FIELD SIMPLE_NAME access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) field_initial_value ( annotations )? )
			{
			match(input,I_FIELD,FOLLOW_I_FIELD_in_field383); 
			match(input, Token.DOWN, null); 
			SIMPLE_NAME20=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_field385); 
			pushFollow(FOLLOW_access_list_in_field387);
			access_list18=access_list();
			state._fsp--;

			match(input,I_FIELD_TYPE,FOLLOW_I_FIELD_TYPE_in_field390); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_nonvoid_type_descriptor_in_field392);
			nonvoid_type_descriptor21=nonvoid_type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 

			pushFollow(FOLLOW_field_initial_value_in_field395);
			field_initial_value19=field_initial_value();
			state._fsp--;

			// smaliTreeWalker.g:257:98: ( annotations )?
			int alt7=2;
			int LA7_0 = input.LA(1);
			if ( (LA7_0==I_ANNOTATIONS) ) {
				alt7=1;
			}
			switch (alt7) {
				case 1 :
					// smaliTreeWalker.g:257:98: annotations
					{
					pushFollow(FOLLOW_annotations_in_field397);
					annotations22=annotations();
					state._fsp--;

					}
					break;

			}

			match(input, Token.UP, null); 


			    int accessFlags = access_list18;


			    if (!AccessFlags.STATIC.isSet(accessFlags) && field_initial_value19 != null) {
			        throw new SemanticException(input, "Initial field values can only be specified for static fields.");
			    }

			    field = dexBuilder.internField(classType, (SIMPLE_NAME20!=null?SIMPLE_NAME20.getText():null), (nonvoid_type_descriptor21!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor21).type:null), access_list18,
			            field_initial_value19, annotations22);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return field;
	}
	// $ANTLR end "field"



	// $ANTLR start "field_initial_value"
	// smaliTreeWalker.g:271:1: field_initial_value returns [EncodedValue encodedValue] : ( ^( I_FIELD_INITIAL_VALUE literal ) |);
	public final EncodedValue field_initial_value() throws RecognitionException {
		EncodedValue encodedValue = null;


		ImmutableEncodedValue literal23 =null;

		try {
			// smaliTreeWalker.g:272:3: ( ^( I_FIELD_INITIAL_VALUE literal ) |)
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==I_FIELD_INITIAL_VALUE) ) {
				alt8=1;
			}
			else if ( (LA8_0==UP||LA8_0==I_ANNOTATIONS) ) {
				alt8=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// smaliTreeWalker.g:272:5: ^( I_FIELD_INITIAL_VALUE literal )
					{
					match(input,I_FIELD_INITIAL_VALUE,FOLLOW_I_FIELD_INITIAL_VALUE_in_field_initial_value418); 
					match(input, Token.DOWN, null); 
					pushFollow(FOLLOW_literal_in_field_initial_value420);
					literal23=literal();
					state._fsp--;

					match(input, Token.UP, null); 

					encodedValue = literal23;
					}
					break;
				case 2 :
					// smaliTreeWalker.g:273:16: 
					{
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return encodedValue;
	}
	// $ANTLR end "field_initial_value"



	// $ANTLR start "literal"
	// smaliTreeWalker.g:275:1: literal returns [ImmutableEncodedValue encodedValue] : ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | string_literal | bool_literal | NULL_LITERAL | type_descriptor | array_literal | subannotation | field_literal | method_literal | enum_literal | method_handle_literal | method_prototype );
	public final ImmutableEncodedValue literal() throws RecognitionException {
		ImmutableEncodedValue encodedValue = null;


		int integer_literal24 =0;
		long long_literal25 =0;
		short short_literal26 =0;
		byte byte_literal27 =0;
		float float_literal28 =0.0f;
		double double_literal29 =0.0;
		char char_literal30 =0;
		String string_literal31 =null;
		boolean bool_literal32 =false;
		String type_descriptor33 =null;
		List<EncodedValue> array_literal34 =null;
		TreeRuleReturnScope subannotation35 =null;
		ImmutableFieldReference field_literal36 =null;
		ImmutableMethodReference method_literal37 =null;
		ImmutableFieldReference enum_literal38 =null;
		ImmutableMethodHandleReference method_handle_literal39 =null;
		ImmutableMethodProtoReference method_prototype40 =null;

		try {
			// smaliTreeWalker.g:276:3: ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | string_literal | bool_literal | NULL_LITERAL | type_descriptor | array_literal | subannotation | field_literal | method_literal | enum_literal | method_handle_literal | method_prototype )
			int alt9=18;
			switch ( input.LA(1) ) {
			case INTEGER_LITERAL:
				{
				alt9=1;
				}
				break;
			case LONG_LITERAL:
				{
				alt9=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt9=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt9=4;
				}
				break;
			case FLOAT_LITERAL:
				{
				alt9=5;
				}
				break;
			case DOUBLE_LITERAL:
				{
				alt9=6;
				}
				break;
			case CHAR_LITERAL:
				{
				alt9=7;
				}
				break;
			case STRING_LITERAL:
				{
				alt9=8;
				}
				break;
			case BOOL_LITERAL:
				{
				alt9=9;
				}
				break;
			case NULL_LITERAL:
				{
				alt9=10;
				}
				break;
			case ARRAY_TYPE_PREFIX:
			case CLASS_DESCRIPTOR:
			case PRIMITIVE_TYPE:
			case VOID_TYPE:
				{
				alt9=11;
				}
				break;
			case I_ENCODED_ARRAY:
				{
				alt9=12;
				}
				break;
			case I_SUBANNOTATION:
				{
				alt9=13;
				}
				break;
			case I_ENCODED_FIELD:
				{
				alt9=14;
				}
				break;
			case I_ENCODED_METHOD:
				{
				alt9=15;
				}
				break;
			case I_ENCODED_ENUM:
				{
				alt9=16;
				}
				break;
			case I_ENCODED_METHOD_HANDLE:
				{
				alt9=17;
				}
				break;
			case I_METHOD_PROTOTYPE:
				{
				alt9=18;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 9, 0, input);
				throw nvae;
			}
			switch (alt9) {
				case 1 :
					// smaliTreeWalker.g:276:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_literal442);
					integer_literal24=integer_literal();
					state._fsp--;

					 encodedValue = new ImmutableIntEncodedValue(integer_literal24); 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:277:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_literal450);
					long_literal25=long_literal();
					state._fsp--;

					 encodedValue = new ImmutableLongEncodedValue(long_literal25); 
					}
					break;
				case 3 :
					// smaliTreeWalker.g:278:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_literal458);
					short_literal26=short_literal();
					state._fsp--;

					 encodedValue = new ImmutableShortEncodedValue(short_literal26); 
					}
					break;
				case 4 :
					// smaliTreeWalker.g:279:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_literal466);
					byte_literal27=byte_literal();
					state._fsp--;

					 encodedValue = new ImmutableByteEncodedValue(byte_literal27); 
					}
					break;
				case 5 :
					// smaliTreeWalker.g:280:5: float_literal
					{
					pushFollow(FOLLOW_float_literal_in_literal474);
					float_literal28=float_literal();
					state._fsp--;

					 encodedValue = new ImmutableFloatEncodedValue(float_literal28); 
					}
					break;
				case 6 :
					// smaliTreeWalker.g:281:5: double_literal
					{
					pushFollow(FOLLOW_double_literal_in_literal482);
					double_literal29=double_literal();
					state._fsp--;

					 encodedValue = new ImmutableDoubleEncodedValue(double_literal29); 
					}
					break;
				case 7 :
					// smaliTreeWalker.g:282:5: char_literal
					{
					pushFollow(FOLLOW_char_literal_in_literal490);
					char_literal30=char_literal();
					state._fsp--;

					 encodedValue = new ImmutableCharEncodedValue(char_literal30); 
					}
					break;
				case 8 :
					// smaliTreeWalker.g:283:5: string_literal
					{
					pushFollow(FOLLOW_string_literal_in_literal498);
					string_literal31=string_literal();
					state._fsp--;

					 encodedValue = new ImmutableStringEncodedValue(string_literal31); 
					}
					break;
				case 9 :
					// smaliTreeWalker.g:284:5: bool_literal
					{
					pushFollow(FOLLOW_bool_literal_in_literal506);
					bool_literal32=bool_literal();
					state._fsp--;

					 encodedValue = ImmutableBooleanEncodedValue.forBoolean(bool_literal32); 
					}
					break;
				case 10 :
					// smaliTreeWalker.g:285:5: NULL_LITERAL
					{
					match(input,NULL_LITERAL,FOLLOW_NULL_LITERAL_in_literal514); 
					 encodedValue = ImmutableNullEncodedValue.INSTANCE; 
					}
					break;
				case 11 :
					// smaliTreeWalker.g:286:5: type_descriptor
					{
					pushFollow(FOLLOW_type_descriptor_in_literal522);
					type_descriptor33=type_descriptor();
					state._fsp--;

					 encodedValue = new ImmutableTypeEncodedValue(type_descriptor33); 
					}
					break;
				case 12 :
					// smaliTreeWalker.g:287:5: array_literal
					{
					pushFollow(FOLLOW_array_literal_in_literal530);
					array_literal34=array_literal();
					state._fsp--;

					 encodedValue = new ImmutableArrayEncodedValue(array_literal34); 
					}
					break;
				case 13 :
					// smaliTreeWalker.g:288:5: subannotation
					{
					pushFollow(FOLLOW_subannotation_in_literal538);
					subannotation35=subannotation();
					state._fsp--;

					 encodedValue = new ImmutableAnnotationEncodedValue((subannotation35!=null?((smaliTreeWalker.subannotation_return)subannotation35).annotationType:null), (subannotation35!=null?((smaliTreeWalker.subannotation_return)subannotation35).elements:null)); 
					}
					break;
				case 14 :
					// smaliTreeWalker.g:289:5: field_literal
					{
					pushFollow(FOLLOW_field_literal_in_literal546);
					field_literal36=field_literal();
					state._fsp--;

					 encodedValue = new ImmutableFieldEncodedValue(field_literal36); 
					}
					break;
				case 15 :
					// smaliTreeWalker.g:290:5: method_literal
					{
					pushFollow(FOLLOW_method_literal_in_literal554);
					method_literal37=method_literal();
					state._fsp--;

					 encodedValue = new ImmutableMethodEncodedValue(method_literal37); 
					}
					break;
				case 16 :
					// smaliTreeWalker.g:291:5: enum_literal
					{
					pushFollow(FOLLOW_enum_literal_in_literal562);
					enum_literal38=enum_literal();
					state._fsp--;

					 encodedValue = new ImmutableEnumEncodedValue(enum_literal38); 
					}
					break;
				case 17 :
					// smaliTreeWalker.g:292:5: method_handle_literal
					{
					pushFollow(FOLLOW_method_handle_literal_in_literal570);
					method_handle_literal39=method_handle_literal();
					state._fsp--;

					 encodedValue = new ImmutableMethodHandleEncodedValue(method_handle_literal39); 
					}
					break;
				case 18 :
					// smaliTreeWalker.g:293:5: method_prototype
					{
					pushFollow(FOLLOW_method_prototype_in_literal578);
					method_prototype40=method_prototype();
					state._fsp--;

					 encodedValue = new ImmutableMethodTypeEncodedValue(method_prototype40); 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return encodedValue;
	}
	// $ANTLR end "literal"



	// $ANTLR start "fixed_64bit_literal_number"
	// smaliTreeWalker.g:296:1: fixed_64bit_literal_number returns [Number value] : ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | bool_literal );
	public final Number fixed_64bit_literal_number() throws RecognitionException {
		Number value = null;


		int integer_literal41 =0;
		long long_literal42 =0;
		short short_literal43 =0;
		byte byte_literal44 =0;
		float float_literal45 =0.0f;
		double double_literal46 =0.0;
		char char_literal47 =0;
		boolean bool_literal48 =false;

		try {
			// smaliTreeWalker.g:297:3: ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | bool_literal )
			int alt10=8;
			switch ( input.LA(1) ) {
			case INTEGER_LITERAL:
				{
				alt10=1;
				}
				break;
			case LONG_LITERAL:
				{
				alt10=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt10=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt10=4;
				}
				break;
			case FLOAT_LITERAL:
				{
				alt10=5;
				}
				break;
			case DOUBLE_LITERAL:
				{
				alt10=6;
				}
				break;
			case CHAR_LITERAL:
				{
				alt10=7;
				}
				break;
			case BOOL_LITERAL:
				{
				alt10=8;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}
			switch (alt10) {
				case 1 :
					// smaliTreeWalker.g:297:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_fixed_64bit_literal_number594);
					integer_literal41=integer_literal();
					state._fsp--;

					 value = integer_literal41; 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:298:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_fixed_64bit_literal_number602);
					long_literal42=long_literal();
					state._fsp--;

					 value = long_literal42; 
					}
					break;
				case 3 :
					// smaliTreeWalker.g:299:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_fixed_64bit_literal_number610);
					short_literal43=short_literal();
					state._fsp--;

					 value = short_literal43; 
					}
					break;
				case 4 :
					// smaliTreeWalker.g:300:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_fixed_64bit_literal_number618);
					byte_literal44=byte_literal();
					state._fsp--;

					 value = byte_literal44; 
					}
					break;
				case 5 :
					// smaliTreeWalker.g:301:5: float_literal
					{
					pushFollow(FOLLOW_float_literal_in_fixed_64bit_literal_number626);
					float_literal45=float_literal();
					state._fsp--;

					 value = Float.floatToRawIntBits(float_literal45); 
					}
					break;
				case 6 :
					// smaliTreeWalker.g:302:5: double_literal
					{
					pushFollow(FOLLOW_double_literal_in_fixed_64bit_literal_number634);
					double_literal46=double_literal();
					state._fsp--;

					 value = Double.doubleToRawLongBits(double_literal46); 
					}
					break;
				case 7 :
					// smaliTreeWalker.g:303:5: char_literal
					{
					pushFollow(FOLLOW_char_literal_in_fixed_64bit_literal_number642);
					char_literal47=char_literal();
					state._fsp--;

					 value = (int)char_literal47; 
					}
					break;
				case 8 :
					// smaliTreeWalker.g:304:5: bool_literal
					{
					pushFollow(FOLLOW_bool_literal_in_fixed_64bit_literal_number650);
					bool_literal48=bool_literal();
					state._fsp--;

					 value = bool_literal48?1:0; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "fixed_64bit_literal_number"



	// $ANTLR start "fixed_64bit_literal"
	// smaliTreeWalker.g:306:1: fixed_64bit_literal returns [long value] : ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | bool_literal );
	public final long fixed_64bit_literal() throws RecognitionException {
		long value = 0;


		int integer_literal49 =0;
		long long_literal50 =0;
		short short_literal51 =0;
		byte byte_literal52 =0;
		float float_literal53 =0.0f;
		double double_literal54 =0.0;
		char char_literal55 =0;
		boolean bool_literal56 =false;

		try {
			// smaliTreeWalker.g:307:3: ( integer_literal | long_literal | short_literal | byte_literal | float_literal | double_literal | char_literal | bool_literal )
			int alt11=8;
			switch ( input.LA(1) ) {
			case INTEGER_LITERAL:
				{
				alt11=1;
				}
				break;
			case LONG_LITERAL:
				{
				alt11=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt11=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt11=4;
				}
				break;
			case FLOAT_LITERAL:
				{
				alt11=5;
				}
				break;
			case DOUBLE_LITERAL:
				{
				alt11=6;
				}
				break;
			case CHAR_LITERAL:
				{
				alt11=7;
				}
				break;
			case BOOL_LITERAL:
				{
				alt11=8;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}
			switch (alt11) {
				case 1 :
					// smaliTreeWalker.g:307:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_fixed_64bit_literal665);
					integer_literal49=integer_literal();
					state._fsp--;

					 value = integer_literal49; 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:308:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_fixed_64bit_literal673);
					long_literal50=long_literal();
					state._fsp--;

					 value = long_literal50; 
					}
					break;
				case 3 :
					// smaliTreeWalker.g:309:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_fixed_64bit_literal681);
					short_literal51=short_literal();
					state._fsp--;

					 value = short_literal51; 
					}
					break;
				case 4 :
					// smaliTreeWalker.g:310:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_fixed_64bit_literal689);
					byte_literal52=byte_literal();
					state._fsp--;

					 value = byte_literal52; 
					}
					break;
				case 5 :
					// smaliTreeWalker.g:311:5: float_literal
					{
					pushFollow(FOLLOW_float_literal_in_fixed_64bit_literal697);
					float_literal53=float_literal();
					state._fsp--;

					 value = Float.floatToRawIntBits(float_literal53); 
					}
					break;
				case 6 :
					// smaliTreeWalker.g:312:5: double_literal
					{
					pushFollow(FOLLOW_double_literal_in_fixed_64bit_literal705);
					double_literal54=double_literal();
					state._fsp--;

					 value = Double.doubleToRawLongBits(double_literal54); 
					}
					break;
				case 7 :
					// smaliTreeWalker.g:313:5: char_literal
					{
					pushFollow(FOLLOW_char_literal_in_fixed_64bit_literal713);
					char_literal55=char_literal();
					state._fsp--;

					 value = char_literal55; 
					}
					break;
				case 8 :
					// smaliTreeWalker.g:314:5: bool_literal
					{
					pushFollow(FOLLOW_bool_literal_in_fixed_64bit_literal721);
					bool_literal56=bool_literal();
					state._fsp--;

					 value = bool_literal56?1:0; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "fixed_64bit_literal"



	// $ANTLR start "fixed_32bit_literal"
	// smaliTreeWalker.g:318:1: fixed_32bit_literal returns [int value] : ( integer_literal | long_literal | short_literal | byte_literal | float_literal | char_literal | bool_literal );
	public final int fixed_32bit_literal() throws RecognitionException {
		int value = 0;


		int integer_literal57 =0;
		long long_literal58 =0;
		short short_literal59 =0;
		byte byte_literal60 =0;
		float float_literal61 =0.0f;
		char char_literal62 =0;
		boolean bool_literal63 =false;

		try {
			// smaliTreeWalker.g:319:3: ( integer_literal | long_literal | short_literal | byte_literal | float_literal | char_literal | bool_literal )
			int alt12=7;
			switch ( input.LA(1) ) {
			case INTEGER_LITERAL:
				{
				alt12=1;
				}
				break;
			case LONG_LITERAL:
				{
				alt12=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt12=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt12=4;
				}
				break;
			case FLOAT_LITERAL:
				{
				alt12=5;
				}
				break;
			case CHAR_LITERAL:
				{
				alt12=6;
				}
				break;
			case BOOL_LITERAL:
				{
				alt12=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 12, 0, input);
				throw nvae;
			}
			switch (alt12) {
				case 1 :
					// smaliTreeWalker.g:319:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_fixed_32bit_literal738);
					integer_literal57=integer_literal();
					state._fsp--;

					 value = integer_literal57; 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:320:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_fixed_32bit_literal746);
					long_literal58=long_literal();
					state._fsp--;

					 LiteralTools.checkInt(long_literal58); value = (int)long_literal58; 
					}
					break;
				case 3 :
					// smaliTreeWalker.g:321:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_fixed_32bit_literal754);
					short_literal59=short_literal();
					state._fsp--;

					 value = short_literal59; 
					}
					break;
				case 4 :
					// smaliTreeWalker.g:322:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_fixed_32bit_literal762);
					byte_literal60=byte_literal();
					state._fsp--;

					 value = byte_literal60; 
					}
					break;
				case 5 :
					// smaliTreeWalker.g:323:5: float_literal
					{
					pushFollow(FOLLOW_float_literal_in_fixed_32bit_literal770);
					float_literal61=float_literal();
					state._fsp--;

					 value = Float.floatToRawIntBits(float_literal61); 
					}
					break;
				case 6 :
					// smaliTreeWalker.g:324:5: char_literal
					{
					pushFollow(FOLLOW_char_literal_in_fixed_32bit_literal778);
					char_literal62=char_literal();
					state._fsp--;

					 value = char_literal62; 
					}
					break;
				case 7 :
					// smaliTreeWalker.g:325:5: bool_literal
					{
					pushFollow(FOLLOW_bool_literal_in_fixed_32bit_literal786);
					bool_literal63=bool_literal();
					state._fsp--;

					 value = bool_literal63?1:0; 
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "fixed_32bit_literal"



	// $ANTLR start "array_elements"
	// smaliTreeWalker.g:327:1: array_elements returns [List<Number> elements] : ^( I_ARRAY_ELEMENTS ( fixed_64bit_literal_number )* ) ;
	public final List<Number> array_elements() throws RecognitionException {
		List<Number> elements = null;


		Number fixed_64bit_literal_number64 =null;

		try {
			// smaliTreeWalker.g:328:3: ( ^( I_ARRAY_ELEMENTS ( fixed_64bit_literal_number )* ) )
			// smaliTreeWalker.g:328:5: ^( I_ARRAY_ELEMENTS ( fixed_64bit_literal_number )* )
			{
			elements = Lists.newArrayList();
			match(input,I_ARRAY_ELEMENTS,FOLLOW_I_ARRAY_ELEMENTS_in_array_elements808); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:330:7: ( fixed_64bit_literal_number )*
				loop13:
				while (true) {
					int alt13=2;
					int LA13_0 = input.LA(1);
					if ( ((LA13_0 >= BOOL_LITERAL && LA13_0 <= BYTE_LITERAL)||LA13_0==CHAR_LITERAL||LA13_0==DOUBLE_LITERAL||LA13_0==FLOAT_LITERAL||LA13_0==INTEGER_LITERAL||LA13_0==LONG_LITERAL||LA13_0==SHORT_LITERAL) ) {
						alt13=1;
					}

					switch (alt13) {
					case 1 :
						// smaliTreeWalker.g:330:8: fixed_64bit_literal_number
						{
						pushFollow(FOLLOW_fixed_64bit_literal_number_in_array_elements817);
						fixed_64bit_literal_number64=fixed_64bit_literal_number();
						state._fsp--;


						        elements.add(fixed_64bit_literal_number64);
						      
						}
						break;

					default :
						break loop13;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return elements;
	}
	// $ANTLR end "array_elements"



	// $ANTLR start "packed_switch_elements"
	// smaliTreeWalker.g:335:1: packed_switch_elements returns [List<Label> elements] : ^( I_PACKED_SWITCH_ELEMENTS ( label_ref )* ) ;
	public final List<Label> packed_switch_elements() throws RecognitionException {
		List<Label> elements = null;


		Label label_ref65 =null;

		elements = Lists.newArrayList();
		try {
			// smaliTreeWalker.g:337:3: ( ^( I_PACKED_SWITCH_ELEMENTS ( label_ref )* ) )
			// smaliTreeWalker.g:338:5: ^( I_PACKED_SWITCH_ELEMENTS ( label_ref )* )
			{
			match(input,I_PACKED_SWITCH_ELEMENTS,FOLLOW_I_PACKED_SWITCH_ELEMENTS_in_packed_switch_elements853); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:339:7: ( label_ref )*
				loop14:
				while (true) {
					int alt14=2;
					int LA14_0 = input.LA(1);
					if ( (LA14_0==SIMPLE_NAME) ) {
						alt14=1;
					}

					switch (alt14) {
					case 1 :
						// smaliTreeWalker.g:339:8: label_ref
						{
						pushFollow(FOLLOW_label_ref_in_packed_switch_elements862);
						label_ref65=label_ref();
						state._fsp--;

						 elements.add(label_ref65); 
						}
						break;

					default :
						break loop14;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return elements;
	}
	// $ANTLR end "packed_switch_elements"



	// $ANTLR start "sparse_switch_elements"
	// smaliTreeWalker.g:342:1: sparse_switch_elements returns [List<SwitchLabelElement> elements] : ^( I_SPARSE_SWITCH_ELEMENTS ( fixed_32bit_literal label_ref )* ) ;
	public final List<SwitchLabelElement> sparse_switch_elements() throws RecognitionException {
		List<SwitchLabelElement> elements = null;


		int fixed_32bit_literal66 =0;
		Label label_ref67 =null;

		elements = Lists.newArrayList();
		try {
			// smaliTreeWalker.g:344:3: ( ^( I_SPARSE_SWITCH_ELEMENTS ( fixed_32bit_literal label_ref )* ) )
			// smaliTreeWalker.g:345:5: ^( I_SPARSE_SWITCH_ELEMENTS ( fixed_32bit_literal label_ref )* )
			{
			match(input,I_SPARSE_SWITCH_ELEMENTS,FOLLOW_I_SPARSE_SWITCH_ELEMENTS_in_sparse_switch_elements897); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:346:8: ( fixed_32bit_literal label_ref )*
				loop15:
				while (true) {
					int alt15=2;
					int LA15_0 = input.LA(1);
					if ( ((LA15_0 >= BOOL_LITERAL && LA15_0 <= BYTE_LITERAL)||LA15_0==CHAR_LITERAL||LA15_0==FLOAT_LITERAL||LA15_0==INTEGER_LITERAL||LA15_0==LONG_LITERAL||LA15_0==SHORT_LITERAL) ) {
						alt15=1;
					}

					switch (alt15) {
					case 1 :
						// smaliTreeWalker.g:346:9: fixed_32bit_literal label_ref
						{
						pushFollow(FOLLOW_fixed_32bit_literal_in_sparse_switch_elements907);
						fixed_32bit_literal66=fixed_32bit_literal();
						state._fsp--;

						pushFollow(FOLLOW_label_ref_in_sparse_switch_elements909);
						label_ref67=label_ref();
						state._fsp--;


						         elements.add(new SwitchLabelElement(fixed_32bit_literal66, label_ref67));
						       
						}
						break;

					default :
						break loop15;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return elements;
	}
	// $ANTLR end "sparse_switch_elements"


	protected static class method_scope {
		boolean isStatic;
		int totalMethodRegisters;
		int methodParameterRegisters;
		MethodImplementationBuilder methodBuilder;
	}
	protected Stack<method_scope> method_stack = new Stack<method_scope>();


	// $ANTLR start "method"
	// smaliTreeWalker.g:352:1: method returns [BuilderMethod ret] : ^( I_METHOD method_name_and_prototype access_list ( ( registers_directive ) |) ordered_method_items catches parameters[$method_name_and_prototype.parameters] annotations ) ;
	public final BuilderMethod method() throws RecognitionException {
		method_stack.push(new method_scope());
		BuilderMethod ret = null;


		CommonTree I_METHOD72=null;
		int access_list68 =0;
		TreeRuleReturnScope method_name_and_prototype69 =null;
		TreeRuleReturnScope registers_directive70 =null;
		List<BuilderTryBlock> catches71 =null;
		Set<Annotation> annotations73 =null;


		    method_stack.peek().totalMethodRegisters = 0;
		    method_stack.peek().methodParameterRegisters = 0;
		    int accessFlags = 0;
		    method_stack.peek().isStatic = false;
		  
		try {
			// smaliTreeWalker.g:367:3: ( ^( I_METHOD method_name_and_prototype access_list ( ( registers_directive ) |) ordered_method_items catches parameters[$method_name_and_prototype.parameters] annotations ) )
			// smaliTreeWalker.g:368:5: ^( I_METHOD method_name_and_prototype access_list ( ( registers_directive ) |) ordered_method_items catches parameters[$method_name_and_prototype.parameters] annotations )
			{
			I_METHOD72=(CommonTree)match(input,I_METHOD,FOLLOW_I_METHOD_in_method961); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_method_name_and_prototype_in_method969);
			method_name_and_prototype69=method_name_and_prototype();
			state._fsp--;

			pushFollow(FOLLOW_access_list_in_method977);
			access_list68=access_list();
			state._fsp--;


			        accessFlags = access_list68;
			        method_stack.peek().isStatic = AccessFlags.STATIC.isSet(accessFlags);
			        method_stack.peek().methodParameterRegisters =
			                MethodUtil.getParameterRegisterCount((method_name_and_prototype69!=null?((smaliTreeWalker.method_name_and_prototype_return)method_name_and_prototype69).parameters:null), method_stack.peek().isStatic);
			      
			// smaliTreeWalker.g:377:7: ( ( registers_directive ) |)
			int alt16=2;
			int LA16_0 = input.LA(1);
			if ( (LA16_0==I_LOCALS||LA16_0==I_REGISTERS) ) {
				alt16=1;
			}
			else if ( (LA16_0==I_ORDERED_METHOD_ITEMS) ) {
				alt16=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}

			switch (alt16) {
				case 1 :
					// smaliTreeWalker.g:378:9: ( registers_directive )
					{
					// smaliTreeWalker.g:378:9: ( registers_directive )
					// smaliTreeWalker.g:378:10: registers_directive
					{
					pushFollow(FOLLOW_registers_directive_in_method1004);
					registers_directive70=registers_directive();
					state._fsp--;


					          if ((registers_directive70!=null?((smaliTreeWalker.registers_directive_return)registers_directive70).isLocalsDirective:false)) {
					            method_stack.peek().totalMethodRegisters = (registers_directive70!=null?((smaliTreeWalker.registers_directive_return)registers_directive70).registers:0) + method_stack.peek().methodParameterRegisters;
					          } else {
					            method_stack.peek().totalMethodRegisters = (registers_directive70!=null?((smaliTreeWalker.registers_directive_return)registers_directive70).registers:0);
					          }

					          method_stack.peek().methodBuilder = new MethodImplementationBuilder(method_stack.peek().totalMethodRegisters);

					        
					}

					}
					break;
				case 2 :
					// smaliTreeWalker.g:391:9: 
					{

					          method_stack.peek().methodBuilder = new MethodImplementationBuilder(0);
					        
					}
					break;

			}

			pushFollow(FOLLOW_ordered_method_items_in_method1061);
			ordered_method_items();
			state._fsp--;

			pushFollow(FOLLOW_catches_in_method1069);
			catches71=catches();
			state._fsp--;

			pushFollow(FOLLOW_parameters_in_method1077);
			parameters((method_name_and_prototype69!=null?((smaliTreeWalker.method_name_and_prototype_return)method_name_and_prototype69).parameters:null));
			state._fsp--;

			pushFollow(FOLLOW_annotations_in_method1086);
			annotations73=annotations();
			state._fsp--;

			match(input, Token.UP, null); 


			    MethodImplementation methodImplementation = null;
			    List<BuilderTryBlock> tryBlocks = catches71;

			    boolean isAbstract = false;
			    boolean isNative = false;

			    if ((accessFlags & AccessFlags.ABSTRACT.getValue()) != 0) {
			      isAbstract = true;
			    } else if ((accessFlags & AccessFlags.NATIVE.getValue()) != 0) {
			      isNative = true;
			    }

			    methodImplementation = method_stack.peek().methodBuilder.getMethodImplementation();

			    if (Iterables.isEmpty(methodImplementation.getInstructions())) {
			      if (!isAbstract && !isNative) {
			        throw new SemanticException(input, I_METHOD72, "A non-abstract/non-native method must have at least 1 instruction");
			      }

			      String methodType;
			      if (isAbstract) {
			        methodType = "an abstract";
			      } else {
			        methodType = "a native";
			      }

			      if ((registers_directive70!=null?((CommonTree)registers_directive70.start):null) != null) {
			        if ((registers_directive70!=null?((smaliTreeWalker.registers_directive_return)registers_directive70).isLocalsDirective:false)) {
			          throw new SemanticException(input, (registers_directive70!=null?((CommonTree)registers_directive70.start):null), "A .locals directive is not valid in %s method", methodType);
			        } else {
			          throw new SemanticException(input, (registers_directive70!=null?((CommonTree)registers_directive70.start):null), "A .registers directive is not valid in %s method", methodType);
			        }
			      }

			      if (methodImplementation.getTryBlocks().size() > 0) {
			        throw new SemanticException(input, I_METHOD72, "try/catch blocks cannot be present in %s method", methodType);
			      }

			      if (!Iterables.isEmpty(methodImplementation.getDebugItems())) {
			        throw new SemanticException(input, I_METHOD72, "debug directives cannot be present in %s method", methodType);
			      }

			      methodImplementation = null;
			    } else {
			      if (isAbstract) {
			        throw new SemanticException(input, I_METHOD72, "An abstract method cannot have any instructions");
			      }
			      if (isNative) {
			        throw new SemanticException(input, I_METHOD72, "A native method cannot have any instructions");
			      }

			      if ((registers_directive70!=null?((CommonTree)registers_directive70.start):null) == null) {
			        throw new SemanticException(input, I_METHOD72, "A .registers or .locals directive must be present for a non-abstract/non-final method");
			      }

			      if (method_stack.peek().totalMethodRegisters < method_stack.peek().methodParameterRegisters) {
			        throw new SemanticException(input, (registers_directive70!=null?((CommonTree)registers_directive70.start):null), "This method requires at least " +
			                Integer.toString(method_stack.peek().methodParameterRegisters) +
			                " registers, for the method parameters");
			      }
			    }

			    ret = dexBuilder.internMethod(
			            classType,
			            (method_name_and_prototype69!=null?((smaliTreeWalker.method_name_and_prototype_return)method_name_and_prototype69).name:null),
			            (method_name_and_prototype69!=null?((smaliTreeWalker.method_name_and_prototype_return)method_name_and_prototype69).parameters:null),
			            (method_name_and_prototype69!=null?((smaliTreeWalker.method_name_and_prototype_return)method_name_and_prototype69).returnType:null),
			            accessFlags,
			            annotations73,
			            methodImplementation);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
			method_stack.pop();
		}
		return ret;
	}
	// $ANTLR end "method"



	// $ANTLR start "method_prototype"
	// smaliTreeWalker.g:473:1: method_prototype returns [ImmutableMethodProtoReference proto] : ^( I_METHOD_PROTOTYPE ^( I_METHOD_RETURN_TYPE type_descriptor ) method_type_list ) ;
	public final ImmutableMethodProtoReference method_prototype() throws RecognitionException {
		ImmutableMethodProtoReference proto = null;


		String type_descriptor74 =null;
		List<String> method_type_list75 =null;

		try {
			// smaliTreeWalker.g:474:3: ( ^( I_METHOD_PROTOTYPE ^( I_METHOD_RETURN_TYPE type_descriptor ) method_type_list ) )
			// smaliTreeWalker.g:474:5: ^( I_METHOD_PROTOTYPE ^( I_METHOD_RETURN_TYPE type_descriptor ) method_type_list )
			{
			match(input,I_METHOD_PROTOTYPE,FOLLOW_I_METHOD_PROTOTYPE_in_method_prototype1110); 
			match(input, Token.DOWN, null); 
			match(input,I_METHOD_RETURN_TYPE,FOLLOW_I_METHOD_RETURN_TYPE_in_method_prototype1113); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_type_descriptor_in_method_prototype1115);
			type_descriptor74=type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 

			pushFollow(FOLLOW_method_type_list_in_method_prototype1118);
			method_type_list75=method_type_list();
			state._fsp--;

			match(input, Token.UP, null); 


			    String returnType = type_descriptor74;
			    List<String> parameters = method_type_list75;
			    proto = new ImmutableMethodProtoReference(parameters, returnType);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return proto;
	}
	// $ANTLR end "method_prototype"


	public static class method_name_and_prototype_return extends TreeRuleReturnScope {
		public String name;
		public List<SmaliMethodParameter> parameters;
		public String returnType;
	};


	// $ANTLR start "method_name_and_prototype"
	// smaliTreeWalker.g:481:1: method_name_and_prototype returns [String name, List<SmaliMethodParameter> parameters, String returnType] : SIMPLE_NAME method_prototype ;
	public final smaliTreeWalker.method_name_and_prototype_return method_name_and_prototype() throws RecognitionException {
		smaliTreeWalker.method_name_and_prototype_return retval = new smaliTreeWalker.method_name_and_prototype_return();
		retval.start = input.LT(1);

		CommonTree SIMPLE_NAME76=null;
		ImmutableMethodProtoReference method_prototype77 =null;

		try {
			// smaliTreeWalker.g:482:3: ( SIMPLE_NAME method_prototype )
			// smaliTreeWalker.g:482:5: SIMPLE_NAME method_prototype
			{
			SIMPLE_NAME76=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_method_name_and_prototype1136); 
			pushFollow(FOLLOW_method_prototype_in_method_name_and_prototype1138);
			method_prototype77=method_prototype();
			state._fsp--;


			    retval.name = (SIMPLE_NAME76!=null?SIMPLE_NAME76.getText():null);
			    retval.parameters = Lists.newArrayList();

			    int paramRegister = 0;
			    for (CharSequence type: method_prototype77.getParameterTypes()) {
			        retval.parameters.add(new SmaliMethodParameter(paramRegister++, type.toString()));
			        char c = type.charAt(0);
			        if (c == 'D' || c == 'J') {
			            paramRegister++;
			        }
			    }
			    retval.returnType = method_prototype77.getReturnType();
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_name_and_prototype"



	// $ANTLR start "method_type_list"
	// smaliTreeWalker.g:498:1: method_type_list returns [List<String> types] : ( nonvoid_type_descriptor )* ;
	public final List<String> method_type_list() throws RecognitionException {
		List<String> types = null;


		TreeRuleReturnScope nonvoid_type_descriptor78 =null;


		    types = Lists.newArrayList();
		  
		try {
			// smaliTreeWalker.g:503:3: ( ( nonvoid_type_descriptor )* )
			// smaliTreeWalker.g:503:5: ( nonvoid_type_descriptor )*
			{
			// smaliTreeWalker.g:503:5: ( nonvoid_type_descriptor )*
			loop17:
			while (true) {
				int alt17=2;
				int LA17_0 = input.LA(1);
				if ( (LA17_0==ARRAY_TYPE_PREFIX||LA17_0==CLASS_DESCRIPTOR||LA17_0==PRIMITIVE_TYPE) ) {
					alt17=1;
				}

				switch (alt17) {
				case 1 :
					// smaliTreeWalker.g:504:7: nonvoid_type_descriptor
					{
					pushFollow(FOLLOW_nonvoid_type_descriptor_in_method_type_list1172);
					nonvoid_type_descriptor78=nonvoid_type_descriptor();
					state._fsp--;


					        types.add((nonvoid_type_descriptor78!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor78).type:null));
					      
					}
					break;

				default :
					break loop17;
				}
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return types;
	}
	// $ANTLR end "method_type_list"



	// $ANTLR start "call_site_reference"
	// smaliTreeWalker.g:510:1: call_site_reference returns [ImmutableCallSiteReference callSiteReference] : ^( I_CALL_SITE_REFERENCE call_site_name= SIMPLE_NAME method_name= string_literal method_prototype call_site_extra_arguments method_reference ) ;
	public final ImmutableCallSiteReference call_site_reference() throws RecognitionException {
		ImmutableCallSiteReference callSiteReference = null;


		CommonTree call_site_name=null;
		String method_name =null;
		ImmutableMethodReference method_reference79 =null;
		ImmutableMethodProtoReference method_prototype80 =null;
		List<ImmutableEncodedValue> call_site_extra_arguments81 =null;

		try {
			// smaliTreeWalker.g:511:3: ( ^( I_CALL_SITE_REFERENCE call_site_name= SIMPLE_NAME method_name= string_literal method_prototype call_site_extra_arguments method_reference ) )
			// smaliTreeWalker.g:512:3: ^( I_CALL_SITE_REFERENCE call_site_name= SIMPLE_NAME method_name= string_literal method_prototype call_site_extra_arguments method_reference )
			{
			match(input,I_CALL_SITE_REFERENCE,FOLLOW_I_CALL_SITE_REFERENCE_in_call_site_reference1203); 
			match(input, Token.DOWN, null); 
			call_site_name=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_call_site_reference1207); 
			pushFollow(FOLLOW_string_literal_in_call_site_reference1211);
			method_name=string_literal();
			state._fsp--;

			pushFollow(FOLLOW_method_prototype_in_call_site_reference1213);
			method_prototype80=method_prototype();
			state._fsp--;

			pushFollow(FOLLOW_call_site_extra_arguments_in_call_site_reference1223);
			call_site_extra_arguments81=call_site_extra_arguments();
			state._fsp--;

			pushFollow(FOLLOW_method_reference_in_call_site_reference1225);
			method_reference79=method_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			        String callSiteName = (call_site_name!=null?call_site_name.getText():null);
			        ImmutableMethodHandleReference methodHandleReference =
			            new ImmutableMethodHandleReference(MethodHandleType.INVOKE_STATIC,
			                method_reference79);
			        callSiteReference = new ImmutableCallSiteReference(
			            callSiteName, methodHandleReference, method_name, method_prototype80,
			            call_site_extra_arguments81);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return callSiteReference;
	}
	// $ANTLR end "call_site_reference"


	public static class method_handle_type_return extends TreeRuleReturnScope {
		public int methodHandleType;
	};


	// $ANTLR start "method_handle_type"
	// smaliTreeWalker.g:524:1: method_handle_type returns [int methodHandleType] : ( METHOD_HANDLE_TYPE_FIELD | METHOD_HANDLE_TYPE_METHOD | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE ) ;
	public final smaliTreeWalker.method_handle_type_return method_handle_type() throws RecognitionException {
		smaliTreeWalker.method_handle_type_return retval = new smaliTreeWalker.method_handle_type_return();
		retval.start = input.LT(1);

		try {
			// smaliTreeWalker.g:525:3: ( ( METHOD_HANDLE_TYPE_FIELD | METHOD_HANDLE_TYPE_METHOD | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE ) )
			// smaliTreeWalker.g:525:5: ( METHOD_HANDLE_TYPE_FIELD | METHOD_HANDLE_TYPE_METHOD | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE )
			{
			if ( input.LA(1)==INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE||(input.LA(1) >= METHOD_HANDLE_TYPE_FIELD && input.LA(1) <= METHOD_HANDLE_TYPE_METHOD) ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}

			    retval.methodHandleType = MethodHandleType.getMethodHandleType(input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(retval.start),input.getTreeAdaptor().getTokenStopIndex(retval.start)));
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_handle_type"



	// $ANTLR start "method_handle_reference"
	// smaliTreeWalker.g:529:1: method_handle_reference returns [ImmutableMethodHandleReference methodHandle] : method_handle_type ( field_reference | method_reference ) ;
	public final ImmutableMethodHandleReference method_handle_reference() throws RecognitionException {
		ImmutableMethodHandleReference methodHandle = null;


		TreeRuleReturnScope field_reference82 =null;
		ImmutableMethodReference method_reference83 =null;
		TreeRuleReturnScope method_handle_type84 =null;

		try {
			// smaliTreeWalker.g:530:3: ( method_handle_type ( field_reference | method_reference ) )
			// smaliTreeWalker.g:530:5: method_handle_type ( field_reference | method_reference )
			{
			pushFollow(FOLLOW_method_handle_type_in_method_handle_reference1270);
			method_handle_type84=method_handle_type();
			state._fsp--;

			// smaliTreeWalker.g:530:24: ( field_reference | method_reference )
			int alt18=2;
			switch ( input.LA(1) ) {
			case CLASS_DESCRIPTOR:
				{
				int LA18_1 = input.LA(2);
				if ( (LA18_1==SIMPLE_NAME) ) {
					int LA18_3 = input.LA(3);
					if ( (LA18_3==ARRAY_TYPE_PREFIX||LA18_3==CLASS_DESCRIPTOR||LA18_3==PRIMITIVE_TYPE) ) {
						alt18=1;
					}
					else if ( (LA18_3==I_METHOD_PROTOTYPE) ) {
						alt18=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 18, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				int LA18_2 = input.LA(2);
				if ( (LA18_2==PRIMITIVE_TYPE) ) {
					int LA18_4 = input.LA(3);
					if ( (LA18_4==SIMPLE_NAME) ) {
						int LA18_3 = input.LA(4);
						if ( (LA18_3==ARRAY_TYPE_PREFIX||LA18_3==CLASS_DESCRIPTOR||LA18_3==PRIMITIVE_TYPE) ) {
							alt18=1;
						}
						else if ( (LA18_3==I_METHOD_PROTOTYPE) ) {
							alt18=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 18, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 18, 4, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA18_2==CLASS_DESCRIPTOR) ) {
					int LA18_5 = input.LA(3);
					if ( (LA18_5==SIMPLE_NAME) ) {
						int LA18_3 = input.LA(4);
						if ( (LA18_3==ARRAY_TYPE_PREFIX||LA18_3==CLASS_DESCRIPTOR||LA18_3==PRIMITIVE_TYPE) ) {
							alt18=1;
						}
						else if ( (LA18_3==I_METHOD_PROTOTYPE) ) {
							alt18=2;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 18, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 18, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SIMPLE_NAME:
				{
				int LA18_3 = input.LA(2);
				if ( (LA18_3==ARRAY_TYPE_PREFIX||LA18_3==CLASS_DESCRIPTOR||LA18_3==PRIMITIVE_TYPE) ) {
					alt18=1;
				}
				else if ( (LA18_3==I_METHOD_PROTOTYPE) ) {
					alt18=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 18, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}
			switch (alt18) {
				case 1 :
					// smaliTreeWalker.g:530:25: field_reference
					{
					pushFollow(FOLLOW_field_reference_in_method_handle_reference1273);
					field_reference82=field_reference();
					state._fsp--;

					}
					break;
				case 2 :
					// smaliTreeWalker.g:530:43: method_reference
					{
					pushFollow(FOLLOW_method_reference_in_method_handle_reference1277);
					method_reference83=method_reference();
					state._fsp--;

					}
					break;

			}


			    ImmutableReference reference;
			    if ((field_reference82!=null?(input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(field_reference82.start),input.getTreeAdaptor().getTokenStopIndex(field_reference82.start))):null) != null) {
			        reference = (field_reference82!=null?((smaliTreeWalker.field_reference_return)field_reference82).fieldReference:null);
			    } else {
			        reference = method_reference83;
			    }
			    methodHandle = new ImmutableMethodHandleReference((method_handle_type84!=null?((smaliTreeWalker.method_handle_type_return)method_handle_type84).methodHandleType:0), reference);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return methodHandle;
	}
	// $ANTLR end "method_handle_reference"



	// $ANTLR start "method_handle_literal"
	// smaliTreeWalker.g:540:1: method_handle_literal returns [ImmutableMethodHandleReference value] : ( I_ENCODED_METHOD_HANDLE method_handle_reference ) ;
	public final ImmutableMethodHandleReference method_handle_literal() throws RecognitionException {
		ImmutableMethodHandleReference value = null;


		ImmutableMethodHandleReference method_handle_reference85 =null;

		try {
			// smaliTreeWalker.g:541:3: ( ( I_ENCODED_METHOD_HANDLE method_handle_reference ) )
			// smaliTreeWalker.g:541:5: ( I_ENCODED_METHOD_HANDLE method_handle_reference )
			{
			// smaliTreeWalker.g:541:5: ( I_ENCODED_METHOD_HANDLE method_handle_reference )
			// smaliTreeWalker.g:541:6: I_ENCODED_METHOD_HANDLE method_handle_reference
			{
			match(input,I_ENCODED_METHOD_HANDLE,FOLLOW_I_ENCODED_METHOD_HANDLE_in_method_handle_literal1294); 
			pushFollow(FOLLOW_method_handle_reference_in_method_handle_literal1296);
			method_handle_reference85=method_handle_reference();
			state._fsp--;

			}


			    value = method_handle_reference85;
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "method_handle_literal"



	// $ANTLR start "method_reference"
	// smaliTreeWalker.g:545:1: method_reference returns [ImmutableMethodReference methodReference] : ( reference_type_descriptor )? SIMPLE_NAME method_prototype ;
	public final ImmutableMethodReference method_reference() throws RecognitionException {
		ImmutableMethodReference methodReference = null;


		CommonTree SIMPLE_NAME87=null;
		TreeRuleReturnScope reference_type_descriptor86 =null;
		ImmutableMethodProtoReference method_prototype88 =null;

		try {
			// smaliTreeWalker.g:546:3: ( ( reference_type_descriptor )? SIMPLE_NAME method_prototype )
			// smaliTreeWalker.g:546:5: ( reference_type_descriptor )? SIMPLE_NAME method_prototype
			{
			// smaliTreeWalker.g:546:5: ( reference_type_descriptor )?
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==ARRAY_TYPE_PREFIX||LA19_0==CLASS_DESCRIPTOR) ) {
				alt19=1;
			}
			switch (alt19) {
				case 1 :
					// smaliTreeWalker.g:546:5: reference_type_descriptor
					{
					pushFollow(FOLLOW_reference_type_descriptor_in_method_reference1312);
					reference_type_descriptor86=reference_type_descriptor();
					state._fsp--;

					}
					break;

			}

			SIMPLE_NAME87=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_method_reference1315); 
			pushFollow(FOLLOW_method_prototype_in_method_reference1317);
			method_prototype88=method_prototype();
			state._fsp--;


			    String type;
			    if ((reference_type_descriptor86!=null?((smaliTreeWalker.reference_type_descriptor_return)reference_type_descriptor86).type:null) == null) {
			        type = classType;
			    } else {
			        type = (reference_type_descriptor86!=null?((smaliTreeWalker.reference_type_descriptor_return)reference_type_descriptor86).type:null);
			    }
			    methodReference = new ImmutableMethodReference(type, (SIMPLE_NAME87!=null?SIMPLE_NAME87.getText():null),
			             method_prototype88.getParameterTypes(), method_prototype88.getReturnType());
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return methodReference;
	}
	// $ANTLR end "method_reference"


	public static class field_reference_return extends TreeRuleReturnScope {
		public ImmutableFieldReference fieldReference;
	};


	// $ANTLR start "field_reference"
	// smaliTreeWalker.g:558:1: field_reference returns [ImmutableFieldReference fieldReference] : ( reference_type_descriptor )? SIMPLE_NAME nonvoid_type_descriptor ;
	public final smaliTreeWalker.field_reference_return field_reference() throws RecognitionException {
		smaliTreeWalker.field_reference_return retval = new smaliTreeWalker.field_reference_return();
		retval.start = input.LT(1);

		CommonTree SIMPLE_NAME90=null;
		TreeRuleReturnScope reference_type_descriptor89 =null;
		TreeRuleReturnScope nonvoid_type_descriptor91 =null;

		try {
			// smaliTreeWalker.g:559:3: ( ( reference_type_descriptor )? SIMPLE_NAME nonvoid_type_descriptor )
			// smaliTreeWalker.g:559:5: ( reference_type_descriptor )? SIMPLE_NAME nonvoid_type_descriptor
			{
			// smaliTreeWalker.g:559:5: ( reference_type_descriptor )?
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==ARRAY_TYPE_PREFIX||LA20_0==CLASS_DESCRIPTOR) ) {
				alt20=1;
			}
			switch (alt20) {
				case 1 :
					// smaliTreeWalker.g:559:5: reference_type_descriptor
					{
					pushFollow(FOLLOW_reference_type_descriptor_in_field_reference1334);
					reference_type_descriptor89=reference_type_descriptor();
					state._fsp--;

					}
					break;

			}

			SIMPLE_NAME90=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_field_reference1337); 
			pushFollow(FOLLOW_nonvoid_type_descriptor_in_field_reference1339);
			nonvoid_type_descriptor91=nonvoid_type_descriptor();
			state._fsp--;


			    String type;
			    if ((reference_type_descriptor89!=null?((smaliTreeWalker.reference_type_descriptor_return)reference_type_descriptor89).type:null) == null) {
			        type = classType;
			    } else {
			        type = (reference_type_descriptor89!=null?((smaliTreeWalker.reference_type_descriptor_return)reference_type_descriptor89).type:null);
			    }
			    retval.fieldReference = new ImmutableFieldReference(type, (SIMPLE_NAME90!=null?SIMPLE_NAME90.getText():null),
			            (nonvoid_type_descriptor91!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor91).type:null));
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "field_reference"


	public static class registers_directive_return extends TreeRuleReturnScope {
		public boolean isLocalsDirective;
		public int registers;
	};


	// $ANTLR start "registers_directive"
	// smaliTreeWalker.g:571:1: registers_directive returns [boolean isLocalsDirective, int registers] : ^( ( I_REGISTERS | I_LOCALS ) short_integral_literal ) ;
	public final smaliTreeWalker.registers_directive_return registers_directive() throws RecognitionException {
		smaliTreeWalker.registers_directive_return retval = new smaliTreeWalker.registers_directive_return();
		retval.start = input.LT(1);

		short short_integral_literal92 =0;

		try {
			// smaliTreeWalker.g:572:3: ( ^( ( I_REGISTERS | I_LOCALS ) short_integral_literal ) )
			// smaliTreeWalker.g:572:5: ^( ( I_REGISTERS | I_LOCALS ) short_integral_literal )
			{
			retval.registers = 0;
			// smaliTreeWalker.g:573:7: ( I_REGISTERS | I_LOCALS )
			int alt21=2;
			int LA21_0 = input.LA(1);
			if ( (LA21_0==I_REGISTERS) ) {
				alt21=1;
			}
			else if ( (LA21_0==I_LOCALS) ) {
				alt21=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}

			switch (alt21) {
				case 1 :
					// smaliTreeWalker.g:573:9: I_REGISTERS
					{
					match(input,I_REGISTERS,FOLLOW_I_REGISTERS_in_registers_directive1365); 
					retval.isLocalsDirective = false;
					}
					break;
				case 2 :
					// smaliTreeWalker.g:574:9: I_LOCALS
					{
					match(input,I_LOCALS,FOLLOW_I_LOCALS_in_registers_directive1377); 
					retval.isLocalsDirective = true;
					}
					break;

			}

			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_short_integral_literal_in_registers_directive1395);
			short_integral_literal92=short_integral_literal();
			state._fsp--;

			retval.registers = short_integral_literal92 & 0xFFFF;
			match(input, Token.UP, null); 

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "registers_directive"



	// $ANTLR start "label_def"
	// smaliTreeWalker.g:579:1: label_def : ^( I_LABEL SIMPLE_NAME ) ;
	public final void label_def() throws RecognitionException {
		CommonTree SIMPLE_NAME93=null;

		try {
			// smaliTreeWalker.g:580:3: ( ^( I_LABEL SIMPLE_NAME ) )
			// smaliTreeWalker.g:580:5: ^( I_LABEL SIMPLE_NAME )
			{
			match(input,I_LABEL,FOLLOW_I_LABEL_in_label_def1415); 
			match(input, Token.DOWN, null); 
			SIMPLE_NAME93=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_label_def1417); 
			match(input, Token.UP, null); 


			    method_stack.peek().methodBuilder.addLabel((SIMPLE_NAME93!=null?SIMPLE_NAME93.getText():null));
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "label_def"



	// $ANTLR start "catches"
	// smaliTreeWalker.g:585:1: catches returns [List<BuilderTryBlock> tryBlocks] : ^( I_CATCHES ( catch_directive )* ( catchall_directive )* ) ;
	public final List<BuilderTryBlock> catches() throws RecognitionException {
		List<BuilderTryBlock> tryBlocks = null;


		tryBlocks = Lists.newArrayList();
		try {
			// smaliTreeWalker.g:587:3: ( ^( I_CATCHES ( catch_directive )* ( catchall_directive )* ) )
			// smaliTreeWalker.g:587:5: ^( I_CATCHES ( catch_directive )* ( catchall_directive )* )
			{
			match(input,I_CATCHES,FOLLOW_I_CATCHES_in_catches1443); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:587:17: ( catch_directive )*
				loop22:
				while (true) {
					int alt22=2;
					int LA22_0 = input.LA(1);
					if ( (LA22_0==I_CATCH) ) {
						alt22=1;
					}

					switch (alt22) {
					case 1 :
						// smaliTreeWalker.g:587:17: catch_directive
						{
						pushFollow(FOLLOW_catch_directive_in_catches1445);
						catch_directive();
						state._fsp--;

						}
						break;

					default :
						break loop22;
					}
				}

				// smaliTreeWalker.g:587:34: ( catchall_directive )*
				loop23:
				while (true) {
					int alt23=2;
					int LA23_0 = input.LA(1);
					if ( (LA23_0==I_CATCHALL) ) {
						alt23=1;
					}

					switch (alt23) {
					case 1 :
						// smaliTreeWalker.g:587:34: catchall_directive
						{
						pushFollow(FOLLOW_catchall_directive_in_catches1448);
						catchall_directive();
						state._fsp--;

						}
						break;

					default :
						break loop23;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return tryBlocks;
	}
	// $ANTLR end "catches"



	// $ANTLR start "catch_directive"
	// smaliTreeWalker.g:589:1: catch_directive : ^( I_CATCH nonvoid_type_descriptor from= label_ref to= label_ref using= label_ref ) ;
	public final void catch_directive() throws RecognitionException {
		Label from =null;
		Label to =null;
		Label using =null;
		TreeRuleReturnScope nonvoid_type_descriptor94 =null;

		try {
			// smaliTreeWalker.g:590:3: ( ^( I_CATCH nonvoid_type_descriptor from= label_ref to= label_ref using= label_ref ) )
			// smaliTreeWalker.g:590:5: ^( I_CATCH nonvoid_type_descriptor from= label_ref to= label_ref using= label_ref )
			{
			match(input,I_CATCH,FOLLOW_I_CATCH_in_catch_directive1461); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_nonvoid_type_descriptor_in_catch_directive1463);
			nonvoid_type_descriptor94=nonvoid_type_descriptor();
			state._fsp--;

			pushFollow(FOLLOW_label_ref_in_catch_directive1467);
			from=label_ref();
			state._fsp--;

			pushFollow(FOLLOW_label_ref_in_catch_directive1471);
			to=label_ref();
			state._fsp--;

			pushFollow(FOLLOW_label_ref_in_catch_directive1475);
			using=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			    method_stack.peek().methodBuilder.addCatch(dexBuilder.internTypeReference((nonvoid_type_descriptor94!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor94).type:null)),
			        from, to, using);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "catch_directive"



	// $ANTLR start "catchall_directive"
	// smaliTreeWalker.g:596:1: catchall_directive : ^( I_CATCHALL from= label_ref to= label_ref using= label_ref ) ;
	public final void catchall_directive() throws RecognitionException {
		Label from =null;
		Label to =null;
		Label using =null;

		try {
			// smaliTreeWalker.g:597:3: ( ^( I_CATCHALL from= label_ref to= label_ref using= label_ref ) )
			// smaliTreeWalker.g:597:5: ^( I_CATCHALL from= label_ref to= label_ref using= label_ref )
			{
			match(input,I_CATCHALL,FOLLOW_I_CATCHALL_in_catchall_directive1491); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_label_ref_in_catchall_directive1495);
			from=label_ref();
			state._fsp--;

			pushFollow(FOLLOW_label_ref_in_catchall_directive1499);
			to=label_ref();
			state._fsp--;

			pushFollow(FOLLOW_label_ref_in_catchall_directive1503);
			using=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			    method_stack.peek().methodBuilder.addCatch(from, to, using);
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "catchall_directive"



	// $ANTLR start "parameters"
	// smaliTreeWalker.g:602:1: parameters[List<SmaliMethodParameter> parameters] : ^( I_PARAMETERS ( parameter[parameters] )* ) ;
	public final void parameters(List<SmaliMethodParameter> parameters) throws RecognitionException {
		try {
			// smaliTreeWalker.g:603:3: ( ^( I_PARAMETERS ( parameter[parameters] )* ) )
			// smaliTreeWalker.g:603:5: ^( I_PARAMETERS ( parameter[parameters] )* )
			{
			match(input,I_PARAMETERS,FOLLOW_I_PARAMETERS_in_parameters1520); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:603:20: ( parameter[parameters] )*
				loop24:
				while (true) {
					int alt24=2;
					int LA24_0 = input.LA(1);
					if ( (LA24_0==I_PARAMETER) ) {
						alt24=1;
					}

					switch (alt24) {
					case 1 :
						// smaliTreeWalker.g:603:21: parameter[parameters]
						{
						pushFollow(FOLLOW_parameter_in_parameters1523);
						parameter(parameters);
						state._fsp--;

						}
						break;

					default :
						break loop24;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "parameters"



	// $ANTLR start "parameter"
	// smaliTreeWalker.g:605:1: parameter[List<SmaliMethodParameter> parameters] : ^( I_PARAMETER REGISTER ( string_literal )? annotations ) ;
	public final void parameter(List<SmaliMethodParameter> parameters) throws RecognitionException {
		CommonTree REGISTER95=null;
		CommonTree I_PARAMETER96=null;
		String string_literal97 =null;
		Set<Annotation> annotations98 =null;

		try {
			// smaliTreeWalker.g:606:3: ( ^( I_PARAMETER REGISTER ( string_literal )? annotations ) )
			// smaliTreeWalker.g:606:5: ^( I_PARAMETER REGISTER ( string_literal )? annotations )
			{
			I_PARAMETER96=(CommonTree)match(input,I_PARAMETER,FOLLOW_I_PARAMETER_in_parameter1539); 
			match(input, Token.DOWN, null); 
			REGISTER95=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_parameter1541); 
			// smaliTreeWalker.g:606:28: ( string_literal )?
			int alt25=2;
			int LA25_0 = input.LA(1);
			if ( (LA25_0==STRING_LITERAL) ) {
				alt25=1;
			}
			switch (alt25) {
				case 1 :
					// smaliTreeWalker.g:606:28: string_literal
					{
					pushFollow(FOLLOW_string_literal_in_parameter1543);
					string_literal97=string_literal();
					state._fsp--;

					}
					break;

			}

			pushFollow(FOLLOW_annotations_in_parameter1546);
			annotations98=annotations();
			state._fsp--;

			match(input, Token.UP, null); 


			        final int registerNumber = parseRegister_short((REGISTER95!=null?REGISTER95.getText():null));
			        int totalMethodRegisters = method_stack.peek().totalMethodRegisters;
			        int methodParameterRegisters = method_stack.peek().methodParameterRegisters;

			        if (registerNumber >= totalMethodRegisters) {
			            throw new SemanticException(input, I_PARAMETER96, "Register %s is larger than the maximum register v%d " +
			                    "for this method", (REGISTER95!=null?REGISTER95.getText():null), totalMethodRegisters-1);
			        }
			        final int indexGuess = registerNumber - (totalMethodRegisters - methodParameterRegisters) - (method_stack.peek().isStatic?0:1);

			        if (indexGuess < 0) {
			            throw new SemanticException(input, I_PARAMETER96, "Register %s is not a parameter register.",
			                    (REGISTER95!=null?REGISTER95.getText():null));
			        }

			        int parameterIndex = LinearSearch.linearSearch(parameters, SmaliMethodParameter.COMPARATOR,
			            new WithRegister() { public int getRegister() { return indexGuess; } },
			                indexGuess);

			        if (parameterIndex < 0) {
			            throw new SemanticException(input, I_PARAMETER96, "Register %s is the second half of a wide parameter.",
			                                (REGISTER95!=null?REGISTER95.getText():null));
			        }

			        SmaliMethodParameter methodParameter = parameters.get(parameterIndex);
			        methodParameter.name = string_literal97;
			        if (annotations98 != null && annotations98.size() > 0) {
			            methodParameter.annotations = annotations98;
			        }
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "parameter"



	// $ANTLR start "debug_directive"
	// smaliTreeWalker.g:639:1: debug_directive : ( line | local | end_local | restart_local | prologue | epilogue | source );
	public final void debug_directive() throws RecognitionException {
		try {
			// smaliTreeWalker.g:640:3: ( line | local | end_local | restart_local | prologue | epilogue | source )
			int alt26=7;
			switch ( input.LA(1) ) {
			case I_LINE:
				{
				alt26=1;
				}
				break;
			case I_LOCAL:
				{
				alt26=2;
				}
				break;
			case I_END_LOCAL:
				{
				alt26=3;
				}
				break;
			case I_RESTART_LOCAL:
				{
				alt26=4;
				}
				break;
			case I_PROLOGUE:
				{
				alt26=5;
				}
				break;
			case I_EPILOGUE:
				{
				alt26=6;
				}
				break;
			case I_SOURCE:
				{
				alt26=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}
			switch (alt26) {
				case 1 :
					// smaliTreeWalker.g:640:5: line
					{
					pushFollow(FOLLOW_line_in_debug_directive1563);
					line();
					state._fsp--;

					}
					break;
				case 2 :
					// smaliTreeWalker.g:641:5: local
					{
					pushFollow(FOLLOW_local_in_debug_directive1569);
					local();
					state._fsp--;

					}
					break;
				case 3 :
					// smaliTreeWalker.g:642:5: end_local
					{
					pushFollow(FOLLOW_end_local_in_debug_directive1575);
					end_local();
					state._fsp--;

					}
					break;
				case 4 :
					// smaliTreeWalker.g:643:5: restart_local
					{
					pushFollow(FOLLOW_restart_local_in_debug_directive1581);
					restart_local();
					state._fsp--;

					}
					break;
				case 5 :
					// smaliTreeWalker.g:644:5: prologue
					{
					pushFollow(FOLLOW_prologue_in_debug_directive1587);
					prologue();
					state._fsp--;

					}
					break;
				case 6 :
					// smaliTreeWalker.g:645:5: epilogue
					{
					pushFollow(FOLLOW_epilogue_in_debug_directive1593);
					epilogue();
					state._fsp--;

					}
					break;
				case 7 :
					// smaliTreeWalker.g:646:5: source
					{
					pushFollow(FOLLOW_source_in_debug_directive1599);
					source();
					state._fsp--;

					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "debug_directive"



	// $ANTLR start "line"
	// smaliTreeWalker.g:648:1: line : ^( I_LINE integral_literal ) ;
	public final void line() throws RecognitionException {
		int integral_literal99 =0;

		try {
			// smaliTreeWalker.g:649:3: ( ^( I_LINE integral_literal ) )
			// smaliTreeWalker.g:649:5: ^( I_LINE integral_literal )
			{
			match(input,I_LINE,FOLLOW_I_LINE_in_line1610); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_integral_literal_in_line1612);
			integral_literal99=integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			        method_stack.peek().methodBuilder.addLineNumber(integral_literal99);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "line"



	// $ANTLR start "local"
	// smaliTreeWalker.g:654:1: local : ^( I_LOCAL REGISTER ( ( NULL_LITERAL |name= string_literal ) ( nonvoid_type_descriptor )? (signature= string_literal )? )? ) ;
	public final void local() throws RecognitionException {
		CommonTree REGISTER100=null;
		String name =null;
		String signature =null;
		TreeRuleReturnScope nonvoid_type_descriptor101 =null;

		try {
			// smaliTreeWalker.g:655:3: ( ^( I_LOCAL REGISTER ( ( NULL_LITERAL |name= string_literal ) ( nonvoid_type_descriptor )? (signature= string_literal )? )? ) )
			// smaliTreeWalker.g:655:5: ^( I_LOCAL REGISTER ( ( NULL_LITERAL |name= string_literal ) ( nonvoid_type_descriptor )? (signature= string_literal )? )? )
			{
			match(input,I_LOCAL,FOLLOW_I_LOCAL_in_local1630); 
			match(input, Token.DOWN, null); 
			REGISTER100=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_local1632); 
			// smaliTreeWalker.g:655:24: ( ( NULL_LITERAL |name= string_literal ) ( nonvoid_type_descriptor )? (signature= string_literal )? )?
			int alt30=2;
			int LA30_0 = input.LA(1);
			if ( (LA30_0==NULL_LITERAL||LA30_0==STRING_LITERAL) ) {
				alt30=1;
			}
			switch (alt30) {
				case 1 :
					// smaliTreeWalker.g:655:25: ( NULL_LITERAL |name= string_literal ) ( nonvoid_type_descriptor )? (signature= string_literal )?
					{
					// smaliTreeWalker.g:655:25: ( NULL_LITERAL |name= string_literal )
					int alt27=2;
					int LA27_0 = input.LA(1);
					if ( (LA27_0==NULL_LITERAL) ) {
						alt27=1;
					}
					else if ( (LA27_0==STRING_LITERAL) ) {
						alt27=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 27, 0, input);
						throw nvae;
					}

					switch (alt27) {
						case 1 :
							// smaliTreeWalker.g:655:26: NULL_LITERAL
							{
							match(input,NULL_LITERAL,FOLLOW_NULL_LITERAL_in_local1636); 
							}
							break;
						case 2 :
							// smaliTreeWalker.g:655:41: name= string_literal
							{
							pushFollow(FOLLOW_string_literal_in_local1642);
							name=string_literal();
							state._fsp--;

							}
							break;

					}

					// smaliTreeWalker.g:655:62: ( nonvoid_type_descriptor )?
					int alt28=2;
					int LA28_0 = input.LA(1);
					if ( (LA28_0==ARRAY_TYPE_PREFIX||LA28_0==CLASS_DESCRIPTOR||LA28_0==PRIMITIVE_TYPE) ) {
						alt28=1;
					}
					switch (alt28) {
						case 1 :
							// smaliTreeWalker.g:655:62: nonvoid_type_descriptor
							{
							pushFollow(FOLLOW_nonvoid_type_descriptor_in_local1645);
							nonvoid_type_descriptor101=nonvoid_type_descriptor();
							state._fsp--;

							}
							break;

					}

					// smaliTreeWalker.g:655:96: (signature= string_literal )?
					int alt29=2;
					int LA29_0 = input.LA(1);
					if ( (LA29_0==STRING_LITERAL) ) {
						alt29=1;
					}
					switch (alt29) {
						case 1 :
							// smaliTreeWalker.g:655:96: signature= string_literal
							{
							pushFollow(FOLLOW_string_literal_in_local1650);
							signature=string_literal();
							state._fsp--;

							}
							break;

					}

					}
					break;

			}

			match(input, Token.UP, null); 


			      int registerNumber = parseRegister_short((REGISTER100!=null?REGISTER100.getText():null));
			      method_stack.peek().methodBuilder.addStartLocal(registerNumber,
			              dexBuilder.internNullableStringReference(name),
			              dexBuilder.internNullableTypeReference((nonvoid_type_descriptor101!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor101).type:null)),
			              dexBuilder.internNullableStringReference(signature));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "local"



	// $ANTLR start "end_local"
	// smaliTreeWalker.g:664:1: end_local : ^( I_END_LOCAL REGISTER ) ;
	public final void end_local() throws RecognitionException {
		CommonTree REGISTER102=null;

		try {
			// smaliTreeWalker.g:665:3: ( ^( I_END_LOCAL REGISTER ) )
			// smaliTreeWalker.g:665:5: ^( I_END_LOCAL REGISTER )
			{
			match(input,I_END_LOCAL,FOLLOW_I_END_LOCAL_in_end_local1671); 
			match(input, Token.DOWN, null); 
			REGISTER102=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_end_local1673); 
			match(input, Token.UP, null); 


			      int registerNumber = parseRegister_short((REGISTER102!=null?REGISTER102.getText():null));
			      method_stack.peek().methodBuilder.addEndLocal(registerNumber);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "end_local"



	// $ANTLR start "restart_local"
	// smaliTreeWalker.g:671:1: restart_local : ^( I_RESTART_LOCAL REGISTER ) ;
	public final void restart_local() throws RecognitionException {
		CommonTree REGISTER103=null;

		try {
			// smaliTreeWalker.g:672:3: ( ^( I_RESTART_LOCAL REGISTER ) )
			// smaliTreeWalker.g:672:5: ^( I_RESTART_LOCAL REGISTER )
			{
			match(input,I_RESTART_LOCAL,FOLLOW_I_RESTART_LOCAL_in_restart_local1691); 
			match(input, Token.DOWN, null); 
			REGISTER103=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_restart_local1693); 
			match(input, Token.UP, null); 


			      int registerNumber = parseRegister_short((REGISTER103!=null?REGISTER103.getText():null));
			      method_stack.peek().methodBuilder.addRestartLocal(registerNumber);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "restart_local"



	// $ANTLR start "prologue"
	// smaliTreeWalker.g:678:1: prologue : I_PROLOGUE ;
	public final void prologue() throws RecognitionException {
		try {
			// smaliTreeWalker.g:679:3: ( I_PROLOGUE )
			// smaliTreeWalker.g:679:5: I_PROLOGUE
			{
			match(input,I_PROLOGUE,FOLLOW_I_PROLOGUE_in_prologue1710); 

			      method_stack.peek().methodBuilder.addPrologue();
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "prologue"



	// $ANTLR start "epilogue"
	// smaliTreeWalker.g:684:1: epilogue : I_EPILOGUE ;
	public final void epilogue() throws RecognitionException {
		try {
			// smaliTreeWalker.g:685:3: ( I_EPILOGUE )
			// smaliTreeWalker.g:685:5: I_EPILOGUE
			{
			match(input,I_EPILOGUE,FOLLOW_I_EPILOGUE_in_epilogue1726); 

			      method_stack.peek().methodBuilder.addEpilogue();
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "epilogue"



	// $ANTLR start "source"
	// smaliTreeWalker.g:690:1: source : ^( I_SOURCE ( string_literal )? ) ;
	public final void source() throws RecognitionException {
		String string_literal104 =null;

		try {
			// smaliTreeWalker.g:691:3: ( ^( I_SOURCE ( string_literal )? ) )
			// smaliTreeWalker.g:691:5: ^( I_SOURCE ( string_literal )? )
			{
			match(input,I_SOURCE,FOLLOW_I_SOURCE_in_source1743); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:691:16: ( string_literal )?
				int alt31=2;
				int LA31_0 = input.LA(1);
				if ( (LA31_0==STRING_LITERAL) ) {
					alt31=1;
				}
				switch (alt31) {
					case 1 :
						// smaliTreeWalker.g:691:16: string_literal
						{
						pushFollow(FOLLOW_string_literal_in_source1745);
						string_literal104=string_literal();
						state._fsp--;

						}
						break;

				}

				match(input, Token.UP, null); 
			}


			      method_stack.peek().methodBuilder.addSetSourceFile(dexBuilder.internNullableStringReference(string_literal104));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "source"



	// $ANTLR start "call_site_extra_arguments"
	// smaliTreeWalker.g:696:1: call_site_extra_arguments returns [List<ImmutableEncodedValue> extraArguments] : ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) ;
	public final List<ImmutableEncodedValue> call_site_extra_arguments() throws RecognitionException {
		List<ImmutableEncodedValue> extraArguments = null;


		ImmutableEncodedValue literal105 =null;

		try {
			// smaliTreeWalker.g:697:3: ( ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) )
			// smaliTreeWalker.g:697:5: ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* )
			{
			 extraArguments = Lists.newArrayList(); 
			match(input,I_CALL_SITE_EXTRA_ARGUMENTS,FOLLOW_I_CALL_SITE_EXTRA_ARGUMENTS_in_call_site_extra_arguments1771); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:698:33: ( literal )*
				loop32:
				while (true) {
					int alt32=2;
					int LA32_0 = input.LA(1);
					if ( (LA32_0==ARRAY_TYPE_PREFIX||(LA32_0 >= BOOL_LITERAL && LA32_0 <= BYTE_LITERAL)||(LA32_0 >= CHAR_LITERAL && LA32_0 <= CLASS_DESCRIPTOR)||LA32_0==DOUBLE_LITERAL||LA32_0==FLOAT_LITERAL||LA32_0==INTEGER_LITERAL||(LA32_0 >= I_ENCODED_ARRAY && LA32_0 <= I_ENCODED_METHOD_HANDLE)||LA32_0==I_METHOD_PROTOTYPE||LA32_0==I_SUBANNOTATION||LA32_0==LONG_LITERAL||LA32_0==NULL_LITERAL||LA32_0==PRIMITIVE_TYPE||LA32_0==SHORT_LITERAL||LA32_0==STRING_LITERAL||LA32_0==VOID_TYPE) ) {
						alt32=1;
					}

					switch (alt32) {
					case 1 :
						// smaliTreeWalker.g:698:34: literal
						{
						pushFollow(FOLLOW_literal_in_call_site_extra_arguments1774);
						literal105=literal();
						state._fsp--;

						 extraArguments.add(literal105); 
						}
						break;

					default :
						break loop32;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return extraArguments;
	}
	// $ANTLR end "call_site_extra_arguments"



	// $ANTLR start "ordered_method_items"
	// smaliTreeWalker.g:700:1: ordered_method_items : ^( I_ORDERED_METHOD_ITEMS ( label_def | instruction | debug_directive )* ) ;
	public final void ordered_method_items() throws RecognitionException {
		try {
			// smaliTreeWalker.g:701:3: ( ^( I_ORDERED_METHOD_ITEMS ( label_def | instruction | debug_directive )* ) )
			// smaliTreeWalker.g:701:5: ^( I_ORDERED_METHOD_ITEMS ( label_def | instruction | debug_directive )* )
			{
			match(input,I_ORDERED_METHOD_ITEMS,FOLLOW_I_ORDERED_METHOD_ITEMS_in_ordered_method_items1790); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:701:30: ( label_def | instruction | debug_directive )*
				loop33:
				while (true) {
					int alt33=4;
					switch ( input.LA(1) ) {
					case I_LABEL:
						{
						alt33=1;
						}
						break;
					case I_STATEMENT_ARRAY_DATA:
					case I_STATEMENT_FORMAT10t:
					case I_STATEMENT_FORMAT10x:
					case I_STATEMENT_FORMAT11n:
					case I_STATEMENT_FORMAT11x:
					case I_STATEMENT_FORMAT12x:
					case I_STATEMENT_FORMAT20bc:
					case I_STATEMENT_FORMAT20t:
					case I_STATEMENT_FORMAT21c_FIELD:
					case I_STATEMENT_FORMAT21c_METHOD_HANDLE:
					case I_STATEMENT_FORMAT21c_METHOD_TYPE:
					case I_STATEMENT_FORMAT21c_STRING:
					case I_STATEMENT_FORMAT21c_TYPE:
					case I_STATEMENT_FORMAT21ih:
					case I_STATEMENT_FORMAT21lh:
					case I_STATEMENT_FORMAT21s:
					case I_STATEMENT_FORMAT21t:
					case I_STATEMENT_FORMAT22b:
					case I_STATEMENT_FORMAT22c_FIELD:
					case I_STATEMENT_FORMAT22c_TYPE:
					case I_STATEMENT_FORMAT22s:
					case I_STATEMENT_FORMAT22t:
					case I_STATEMENT_FORMAT22x:
					case I_STATEMENT_FORMAT23x:
					case I_STATEMENT_FORMAT30t:
					case I_STATEMENT_FORMAT31c:
					case I_STATEMENT_FORMAT31i:
					case I_STATEMENT_FORMAT31t:
					case I_STATEMENT_FORMAT32x:
					case I_STATEMENT_FORMAT35c_CALL_SITE:
					case I_STATEMENT_FORMAT35c_METHOD:
					case I_STATEMENT_FORMAT35c_TYPE:
					case I_STATEMENT_FORMAT3rc_CALL_SITE:
					case I_STATEMENT_FORMAT3rc_METHOD:
					case I_STATEMENT_FORMAT3rc_TYPE:
					case I_STATEMENT_FORMAT45cc_METHOD:
					case I_STATEMENT_FORMAT4rcc_METHOD:
					case I_STATEMENT_FORMAT51l:
					case I_STATEMENT_PACKED_SWITCH:
					case I_STATEMENT_SPARSE_SWITCH:
						{
						alt33=2;
						}
						break;
					case I_END_LOCAL:
					case I_EPILOGUE:
					case I_LINE:
					case I_LOCAL:
					case I_PROLOGUE:
					case I_RESTART_LOCAL:
					case I_SOURCE:
						{
						alt33=3;
						}
						break;
					}
					switch (alt33) {
					case 1 :
						// smaliTreeWalker.g:701:31: label_def
						{
						pushFollow(FOLLOW_label_def_in_ordered_method_items1793);
						label_def();
						state._fsp--;

						}
						break;
					case 2 :
						// smaliTreeWalker.g:701:43: instruction
						{
						pushFollow(FOLLOW_instruction_in_ordered_method_items1797);
						instruction();
						state._fsp--;

						}
						break;
					case 3 :
						// smaliTreeWalker.g:701:57: debug_directive
						{
						pushFollow(FOLLOW_debug_directive_in_ordered_method_items1801);
						debug_directive();
						state._fsp--;

						}
						break;

					default :
						break loop33;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "ordered_method_items"



	// $ANTLR start "label_ref"
	// smaliTreeWalker.g:703:1: label_ref returns [Label label] : SIMPLE_NAME ;
	public final Label label_ref() throws RecognitionException {
		Label label = null;


		CommonTree SIMPLE_NAME106=null;

		try {
			// smaliTreeWalker.g:704:3: ( SIMPLE_NAME )
			// smaliTreeWalker.g:704:5: SIMPLE_NAME
			{
			SIMPLE_NAME106=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_label_ref1817); 
			 label = method_stack.peek().methodBuilder.getLabel((SIMPLE_NAME106!=null?SIMPLE_NAME106.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return label;
	}
	// $ANTLR end "label_ref"


	public static class register_list_return extends TreeRuleReturnScope {
		public byte[] registers;
		public byte registerCount;
	};


	// $ANTLR start "register_list"
	// smaliTreeWalker.g:706:1: register_list returns [byte[] registers, byte registerCount] : ^( I_REGISTER_LIST ( REGISTER )* ) ;
	public final smaliTreeWalker.register_list_return register_list() throws RecognitionException {
		smaliTreeWalker.register_list_return retval = new smaliTreeWalker.register_list_return();
		retval.start = input.LT(1);

		CommonTree I_REGISTER_LIST107=null;
		CommonTree REGISTER108=null;


		    retval.registers = new byte[5];
		    retval.registerCount = 0;
		  
		try {
			// smaliTreeWalker.g:712:3: ( ^( I_REGISTER_LIST ( REGISTER )* ) )
			// smaliTreeWalker.g:712:5: ^( I_REGISTER_LIST ( REGISTER )* )
			{
			I_REGISTER_LIST107=(CommonTree)match(input,I_REGISTER_LIST,FOLLOW_I_REGISTER_LIST_in_register_list1842); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:713:7: ( REGISTER )*
				loop34:
				while (true) {
					int alt34=2;
					int LA34_0 = input.LA(1);
					if ( (LA34_0==REGISTER) ) {
						alt34=1;
					}

					switch (alt34) {
					case 1 :
						// smaliTreeWalker.g:713:8: REGISTER
						{
						REGISTER108=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_register_list1851); 

						        if (retval.registerCount == 5) {
						          throw new SemanticException(input, I_REGISTER_LIST107, "A list of registers can only have a maximum of 5 " +
						                  "registers. Use the <op>/range alternate opcode instead.");
						        }
						        retval.registers[retval.registerCount++] = parseRegister_nibble((REGISTER108!=null?REGISTER108.getText():null));
						      
						}
						break;

					default :
						break loop34;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "register_list"


	public static class register_range_return extends TreeRuleReturnScope {
		public int startRegister;
		public int endRegister;
	};


	// $ANTLR start "register_range"
	// smaliTreeWalker.g:722:1: register_range returns [int startRegister, int endRegister] : ^( I_REGISTER_RANGE (startReg= REGISTER (endReg= REGISTER )? )? ) ;
	public final smaliTreeWalker.register_range_return register_range() throws RecognitionException {
		smaliTreeWalker.register_range_return retval = new smaliTreeWalker.register_range_return();
		retval.start = input.LT(1);

		CommonTree startReg=null;
		CommonTree endReg=null;
		CommonTree I_REGISTER_RANGE109=null;

		try {
			// smaliTreeWalker.g:723:3: ( ^( I_REGISTER_RANGE (startReg= REGISTER (endReg= REGISTER )? )? ) )
			// smaliTreeWalker.g:723:5: ^( I_REGISTER_RANGE (startReg= REGISTER (endReg= REGISTER )? )? )
			{
			I_REGISTER_RANGE109=(CommonTree)match(input,I_REGISTER_RANGE,FOLLOW_I_REGISTER_RANGE_in_register_range1876); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:723:24: (startReg= REGISTER (endReg= REGISTER )? )?
				int alt36=2;
				int LA36_0 = input.LA(1);
				if ( (LA36_0==REGISTER) ) {
					alt36=1;
				}
				switch (alt36) {
					case 1 :
						// smaliTreeWalker.g:723:25: startReg= REGISTER (endReg= REGISTER )?
						{
						startReg=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_register_range1881); 
						// smaliTreeWalker.g:723:49: (endReg= REGISTER )?
						int alt35=2;
						int LA35_0 = input.LA(1);
						if ( (LA35_0==REGISTER) ) {
							alt35=1;
						}
						switch (alt35) {
							case 1 :
								// smaliTreeWalker.g:723:49: endReg= REGISTER
								{
								endReg=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_register_range1885); 
								}
								break;

						}

						}
						break;

				}

				match(input, Token.UP, null); 
			}


			        if (startReg == null) {
			            retval.startRegister = 0;
			            retval.endRegister = -1;
			        } else {
			                retval.startRegister = parseRegister_short((startReg!=null?startReg.getText():null));
			                if (endReg == null) {
			                    retval.endRegister = retval.startRegister;
			                } else {
			                    retval.endRegister = parseRegister_short((endReg!=null?endReg.getText():null));
			                }

			                int registerCount = retval.endRegister-retval.startRegister+1;
			                if (registerCount < 1) {
			                    throw new SemanticException(input, I_REGISTER_RANGE109, "A register range must have the lower register listed first");
			                }
			            }
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "register_range"



	// $ANTLR start "verification_error_reference"
	// smaliTreeWalker.g:743:1: verification_error_reference returns [ImmutableReference reference] : ( CLASS_DESCRIPTOR | field_reference | method_reference );
	public final ImmutableReference verification_error_reference() throws RecognitionException {
		ImmutableReference reference = null;


		CommonTree CLASS_DESCRIPTOR110=null;
		TreeRuleReturnScope field_reference111 =null;
		ImmutableMethodReference method_reference112 =null;

		try {
			// smaliTreeWalker.g:744:3: ( CLASS_DESCRIPTOR | field_reference | method_reference )
			int alt37=3;
			switch ( input.LA(1) ) {
			case CLASS_DESCRIPTOR:
				{
				int LA37_1 = input.LA(2);
				if ( (LA37_1==UP) ) {
					alt37=1;
				}
				else if ( (LA37_1==SIMPLE_NAME) ) {
					int LA37_3 = input.LA(3);
					if ( (LA37_3==ARRAY_TYPE_PREFIX||LA37_3==CLASS_DESCRIPTOR||LA37_3==PRIMITIVE_TYPE) ) {
						alt37=2;
					}
					else if ( (LA37_3==I_METHOD_PROTOTYPE) ) {
						alt37=3;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 37, 3, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 37, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				int LA37_2 = input.LA(2);
				if ( (LA37_2==PRIMITIVE_TYPE) ) {
					int LA37_5 = input.LA(3);
					if ( (LA37_5==SIMPLE_NAME) ) {
						int LA37_3 = input.LA(4);
						if ( (LA37_3==ARRAY_TYPE_PREFIX||LA37_3==CLASS_DESCRIPTOR||LA37_3==PRIMITIVE_TYPE) ) {
							alt37=2;
						}
						else if ( (LA37_3==I_METHOD_PROTOTYPE) ) {
							alt37=3;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 37, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 37, 5, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}
				else if ( (LA37_2==CLASS_DESCRIPTOR) ) {
					int LA37_6 = input.LA(3);
					if ( (LA37_6==SIMPLE_NAME) ) {
						int LA37_3 = input.LA(4);
						if ( (LA37_3==ARRAY_TYPE_PREFIX||LA37_3==CLASS_DESCRIPTOR||LA37_3==PRIMITIVE_TYPE) ) {
							alt37=2;
						}
						else if ( (LA37_3==I_METHOD_PROTOTYPE) ) {
							alt37=3;
						}

						else {
							int nvaeMark = input.mark();
							try {
								for (int nvaeConsume = 0; nvaeConsume < 4 - 1; nvaeConsume++) {
									input.consume();
								}
								NoViableAltException nvae =
									new NoViableAltException("", 37, 3, input);
								throw nvae;
							} finally {
								input.rewind(nvaeMark);
							}
						}

					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 37, 6, input);
							throw nvae;
						} finally {
							input.rewind(nvaeMark);
						}
					}

				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 37, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SIMPLE_NAME:
				{
				int LA37_3 = input.LA(2);
				if ( (LA37_3==ARRAY_TYPE_PREFIX||LA37_3==CLASS_DESCRIPTOR||LA37_3==PRIMITIVE_TYPE) ) {
					alt37=2;
				}
				else if ( (LA37_3==I_METHOD_PROTOTYPE) ) {
					alt37=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 37, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 37, 0, input);
				throw nvae;
			}
			switch (alt37) {
				case 1 :
					// smaliTreeWalker.g:744:5: CLASS_DESCRIPTOR
					{
					CLASS_DESCRIPTOR110=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_verification_error_reference1908); 

					    reference = new ImmutableTypeReference((CLASS_DESCRIPTOR110!=null?CLASS_DESCRIPTOR110.getText():null));
					  
					}
					break;
				case 2 :
					// smaliTreeWalker.g:748:5: field_reference
					{
					pushFollow(FOLLOW_field_reference_in_verification_error_reference1918);
					field_reference111=field_reference();
					state._fsp--;


					    reference = (field_reference111!=null?((smaliTreeWalker.field_reference_return)field_reference111).fieldReference:null);
					  
					}
					break;
				case 3 :
					// smaliTreeWalker.g:752:5: method_reference
					{
					pushFollow(FOLLOW_method_reference_in_verification_error_reference1928);
					method_reference112=method_reference();
					state._fsp--;


					    reference = method_reference112;
					  
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return reference;
	}
	// $ANTLR end "verification_error_reference"



	// $ANTLR start "verification_error_type"
	// smaliTreeWalker.g:757:1: verification_error_type returns [int verificationError] : VERIFICATION_ERROR_TYPE ;
	public final int verification_error_type() throws RecognitionException {
		int verificationError = 0;


		CommonTree VERIFICATION_ERROR_TYPE113=null;

		try {
			// smaliTreeWalker.g:758:3: ( VERIFICATION_ERROR_TYPE )
			// smaliTreeWalker.g:758:5: VERIFICATION_ERROR_TYPE
			{
			VERIFICATION_ERROR_TYPE113=(CommonTree)match(input,VERIFICATION_ERROR_TYPE,FOLLOW_VERIFICATION_ERROR_TYPE_in_verification_error_type1945); 

			    verificationError = VerificationError.getVerificationError((VERIFICATION_ERROR_TYPE113!=null?VERIFICATION_ERROR_TYPE113.getText():null));
			  
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return verificationError;
	}
	// $ANTLR end "verification_error_type"


	public static class instruction_return extends TreeRuleReturnScope {
	};


	// $ANTLR start "instruction"
	// smaliTreeWalker.g:763:1: instruction : ( insn_format10t | insn_format10x | insn_format11n | insn_format11x | insn_format12x | insn_format20bc | insn_format20t | insn_format21c_field | insn_format21c_method_handle | insn_format21c_method_type | insn_format21c_string | insn_format21c_type | insn_format21ih | insn_format21lh | insn_format21s | insn_format21t | insn_format22b | insn_format22c_field | insn_format22c_type | insn_format22s | insn_format22t | insn_format22x | insn_format23x | insn_format30t | insn_format31c | insn_format31i | insn_format31t | insn_format32x | insn_format35c_call_site | insn_format35c_method | insn_format35c_type | insn_format3rc_call_site | insn_format3rc_method | insn_format3rc_type | insn_format45cc_method | insn_format4rcc_method | insn_format51l_type | insn_array_data_directive | insn_packed_switch_directive | insn_sparse_switch_directive );
	public final smaliTreeWalker.instruction_return instruction() throws RecognitionException {
		smaliTreeWalker.instruction_return retval = new smaliTreeWalker.instruction_return();
		retval.start = input.LT(1);

		try {
			// smaliTreeWalker.g:764:3: ( insn_format10t | insn_format10x | insn_format11n | insn_format11x | insn_format12x | insn_format20bc | insn_format20t | insn_format21c_field | insn_format21c_method_handle | insn_format21c_method_type | insn_format21c_string | insn_format21c_type | insn_format21ih | insn_format21lh | insn_format21s | insn_format21t | insn_format22b | insn_format22c_field | insn_format22c_type | insn_format22s | insn_format22t | insn_format22x | insn_format23x | insn_format30t | insn_format31c | insn_format31i | insn_format31t | insn_format32x | insn_format35c_call_site | insn_format35c_method | insn_format35c_type | insn_format3rc_call_site | insn_format3rc_method | insn_format3rc_type | insn_format45cc_method | insn_format4rcc_method | insn_format51l_type | insn_array_data_directive | insn_packed_switch_directive | insn_sparse_switch_directive )
			int alt38=40;
			switch ( input.LA(1) ) {
			case I_STATEMENT_FORMAT10t:
				{
				alt38=1;
				}
				break;
			case I_STATEMENT_FORMAT10x:
				{
				alt38=2;
				}
				break;
			case I_STATEMENT_FORMAT11n:
				{
				alt38=3;
				}
				break;
			case I_STATEMENT_FORMAT11x:
				{
				alt38=4;
				}
				break;
			case I_STATEMENT_FORMAT12x:
				{
				alt38=5;
				}
				break;
			case I_STATEMENT_FORMAT20bc:
				{
				alt38=6;
				}
				break;
			case I_STATEMENT_FORMAT20t:
				{
				alt38=7;
				}
				break;
			case I_STATEMENT_FORMAT21c_FIELD:
				{
				alt38=8;
				}
				break;
			case I_STATEMENT_FORMAT21c_METHOD_HANDLE:
				{
				alt38=9;
				}
				break;
			case I_STATEMENT_FORMAT21c_METHOD_TYPE:
				{
				alt38=10;
				}
				break;
			case I_STATEMENT_FORMAT21c_STRING:
				{
				alt38=11;
				}
				break;
			case I_STATEMENT_FORMAT21c_TYPE:
				{
				alt38=12;
				}
				break;
			case I_STATEMENT_FORMAT21ih:
				{
				alt38=13;
				}
				break;
			case I_STATEMENT_FORMAT21lh:
				{
				alt38=14;
				}
				break;
			case I_STATEMENT_FORMAT21s:
				{
				alt38=15;
				}
				break;
			case I_STATEMENT_FORMAT21t:
				{
				alt38=16;
				}
				break;
			case I_STATEMENT_FORMAT22b:
				{
				alt38=17;
				}
				break;
			case I_STATEMENT_FORMAT22c_FIELD:
				{
				alt38=18;
				}
				break;
			case I_STATEMENT_FORMAT22c_TYPE:
				{
				alt38=19;
				}
				break;
			case I_STATEMENT_FORMAT22s:
				{
				alt38=20;
				}
				break;
			case I_STATEMENT_FORMAT22t:
				{
				alt38=21;
				}
				break;
			case I_STATEMENT_FORMAT22x:
				{
				alt38=22;
				}
				break;
			case I_STATEMENT_FORMAT23x:
				{
				alt38=23;
				}
				break;
			case I_STATEMENT_FORMAT30t:
				{
				alt38=24;
				}
				break;
			case I_STATEMENT_FORMAT31c:
				{
				alt38=25;
				}
				break;
			case I_STATEMENT_FORMAT31i:
				{
				alt38=26;
				}
				break;
			case I_STATEMENT_FORMAT31t:
				{
				alt38=27;
				}
				break;
			case I_STATEMENT_FORMAT32x:
				{
				alt38=28;
				}
				break;
			case I_STATEMENT_FORMAT35c_CALL_SITE:
				{
				alt38=29;
				}
				break;
			case I_STATEMENT_FORMAT35c_METHOD:
				{
				alt38=30;
				}
				break;
			case I_STATEMENT_FORMAT35c_TYPE:
				{
				alt38=31;
				}
				break;
			case I_STATEMENT_FORMAT3rc_CALL_SITE:
				{
				alt38=32;
				}
				break;
			case I_STATEMENT_FORMAT3rc_METHOD:
				{
				alt38=33;
				}
				break;
			case I_STATEMENT_FORMAT3rc_TYPE:
				{
				alt38=34;
				}
				break;
			case I_STATEMENT_FORMAT45cc_METHOD:
				{
				alt38=35;
				}
				break;
			case I_STATEMENT_FORMAT4rcc_METHOD:
				{
				alt38=36;
				}
				break;
			case I_STATEMENT_FORMAT51l:
				{
				alt38=37;
				}
				break;
			case I_STATEMENT_ARRAY_DATA:
				{
				alt38=38;
				}
				break;
			case I_STATEMENT_PACKED_SWITCH:
				{
				alt38=39;
				}
				break;
			case I_STATEMENT_SPARSE_SWITCH:
				{
				alt38=40;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 38, 0, input);
				throw nvae;
			}
			switch (alt38) {
				case 1 :
					// smaliTreeWalker.g:764:5: insn_format10t
					{
					pushFollow(FOLLOW_insn_format10t_in_instruction1959);
					insn_format10t();
					state._fsp--;

					}
					break;
				case 2 :
					// smaliTreeWalker.g:765:5: insn_format10x
					{
					pushFollow(FOLLOW_insn_format10x_in_instruction1965);
					insn_format10x();
					state._fsp--;

					}
					break;
				case 3 :
					// smaliTreeWalker.g:766:5: insn_format11n
					{
					pushFollow(FOLLOW_insn_format11n_in_instruction1971);
					insn_format11n();
					state._fsp--;

					}
					break;
				case 4 :
					// smaliTreeWalker.g:767:5: insn_format11x
					{
					pushFollow(FOLLOW_insn_format11x_in_instruction1977);
					insn_format11x();
					state._fsp--;

					}
					break;
				case 5 :
					// smaliTreeWalker.g:768:5: insn_format12x
					{
					pushFollow(FOLLOW_insn_format12x_in_instruction1983);
					insn_format12x();
					state._fsp--;

					}
					break;
				case 6 :
					// smaliTreeWalker.g:769:5: insn_format20bc
					{
					pushFollow(FOLLOW_insn_format20bc_in_instruction1989);
					insn_format20bc();
					state._fsp--;

					}
					break;
				case 7 :
					// smaliTreeWalker.g:770:5: insn_format20t
					{
					pushFollow(FOLLOW_insn_format20t_in_instruction1995);
					insn_format20t();
					state._fsp--;

					}
					break;
				case 8 :
					// smaliTreeWalker.g:771:5: insn_format21c_field
					{
					pushFollow(FOLLOW_insn_format21c_field_in_instruction2001);
					insn_format21c_field();
					state._fsp--;

					}
					break;
				case 9 :
					// smaliTreeWalker.g:772:5: insn_format21c_method_handle
					{
					pushFollow(FOLLOW_insn_format21c_method_handle_in_instruction2007);
					insn_format21c_method_handle();
					state._fsp--;

					}
					break;
				case 10 :
					// smaliTreeWalker.g:773:5: insn_format21c_method_type
					{
					pushFollow(FOLLOW_insn_format21c_method_type_in_instruction2013);
					insn_format21c_method_type();
					state._fsp--;

					}
					break;
				case 11 :
					// smaliTreeWalker.g:774:5: insn_format21c_string
					{
					pushFollow(FOLLOW_insn_format21c_string_in_instruction2019);
					insn_format21c_string();
					state._fsp--;

					}
					break;
				case 12 :
					// smaliTreeWalker.g:775:5: insn_format21c_type
					{
					pushFollow(FOLLOW_insn_format21c_type_in_instruction2025);
					insn_format21c_type();
					state._fsp--;

					}
					break;
				case 13 :
					// smaliTreeWalker.g:776:5: insn_format21ih
					{
					pushFollow(FOLLOW_insn_format21ih_in_instruction2031);
					insn_format21ih();
					state._fsp--;

					}
					break;
				case 14 :
					// smaliTreeWalker.g:777:5: insn_format21lh
					{
					pushFollow(FOLLOW_insn_format21lh_in_instruction2037);
					insn_format21lh();
					state._fsp--;

					}
					break;
				case 15 :
					// smaliTreeWalker.g:778:5: insn_format21s
					{
					pushFollow(FOLLOW_insn_format21s_in_instruction2043);
					insn_format21s();
					state._fsp--;

					}
					break;
				case 16 :
					// smaliTreeWalker.g:779:5: insn_format21t
					{
					pushFollow(FOLLOW_insn_format21t_in_instruction2049);
					insn_format21t();
					state._fsp--;

					}
					break;
				case 17 :
					// smaliTreeWalker.g:780:5: insn_format22b
					{
					pushFollow(FOLLOW_insn_format22b_in_instruction2055);
					insn_format22b();
					state._fsp--;

					}
					break;
				case 18 :
					// smaliTreeWalker.g:781:5: insn_format22c_field
					{
					pushFollow(FOLLOW_insn_format22c_field_in_instruction2061);
					insn_format22c_field();
					state._fsp--;

					}
					break;
				case 19 :
					// smaliTreeWalker.g:782:5: insn_format22c_type
					{
					pushFollow(FOLLOW_insn_format22c_type_in_instruction2067);
					insn_format22c_type();
					state._fsp--;

					}
					break;
				case 20 :
					// smaliTreeWalker.g:783:5: insn_format22s
					{
					pushFollow(FOLLOW_insn_format22s_in_instruction2073);
					insn_format22s();
					state._fsp--;

					}
					break;
				case 21 :
					// smaliTreeWalker.g:784:5: insn_format22t
					{
					pushFollow(FOLLOW_insn_format22t_in_instruction2079);
					insn_format22t();
					state._fsp--;

					}
					break;
				case 22 :
					// smaliTreeWalker.g:785:5: insn_format22x
					{
					pushFollow(FOLLOW_insn_format22x_in_instruction2085);
					insn_format22x();
					state._fsp--;

					}
					break;
				case 23 :
					// smaliTreeWalker.g:786:5: insn_format23x
					{
					pushFollow(FOLLOW_insn_format23x_in_instruction2091);
					insn_format23x();
					state._fsp--;

					}
					break;
				case 24 :
					// smaliTreeWalker.g:787:5: insn_format30t
					{
					pushFollow(FOLLOW_insn_format30t_in_instruction2097);
					insn_format30t();
					state._fsp--;

					}
					break;
				case 25 :
					// smaliTreeWalker.g:788:5: insn_format31c
					{
					pushFollow(FOLLOW_insn_format31c_in_instruction2103);
					insn_format31c();
					state._fsp--;

					}
					break;
				case 26 :
					// smaliTreeWalker.g:789:5: insn_format31i
					{
					pushFollow(FOLLOW_insn_format31i_in_instruction2109);
					insn_format31i();
					state._fsp--;

					}
					break;
				case 27 :
					// smaliTreeWalker.g:790:5: insn_format31t
					{
					pushFollow(FOLLOW_insn_format31t_in_instruction2115);
					insn_format31t();
					state._fsp--;

					}
					break;
				case 28 :
					// smaliTreeWalker.g:791:5: insn_format32x
					{
					pushFollow(FOLLOW_insn_format32x_in_instruction2121);
					insn_format32x();
					state._fsp--;

					}
					break;
				case 29 :
					// smaliTreeWalker.g:792:5: insn_format35c_call_site
					{
					pushFollow(FOLLOW_insn_format35c_call_site_in_instruction2127);
					insn_format35c_call_site();
					state._fsp--;

					}
					break;
				case 30 :
					// smaliTreeWalker.g:793:5: insn_format35c_method
					{
					pushFollow(FOLLOW_insn_format35c_method_in_instruction2133);
					insn_format35c_method();
					state._fsp--;

					}
					break;
				case 31 :
					// smaliTreeWalker.g:794:5: insn_format35c_type
					{
					pushFollow(FOLLOW_insn_format35c_type_in_instruction2139);
					insn_format35c_type();
					state._fsp--;

					}
					break;
				case 32 :
					// smaliTreeWalker.g:795:5: insn_format3rc_call_site
					{
					pushFollow(FOLLOW_insn_format3rc_call_site_in_instruction2145);
					insn_format3rc_call_site();
					state._fsp--;

					}
					break;
				case 33 :
					// smaliTreeWalker.g:796:5: insn_format3rc_method
					{
					pushFollow(FOLLOW_insn_format3rc_method_in_instruction2151);
					insn_format3rc_method();
					state._fsp--;

					}
					break;
				case 34 :
					// smaliTreeWalker.g:797:5: insn_format3rc_type
					{
					pushFollow(FOLLOW_insn_format3rc_type_in_instruction2157);
					insn_format3rc_type();
					state._fsp--;

					}
					break;
				case 35 :
					// smaliTreeWalker.g:798:5: insn_format45cc_method
					{
					pushFollow(FOLLOW_insn_format45cc_method_in_instruction2163);
					insn_format45cc_method();
					state._fsp--;

					}
					break;
				case 36 :
					// smaliTreeWalker.g:799:5: insn_format4rcc_method
					{
					pushFollow(FOLLOW_insn_format4rcc_method_in_instruction2169);
					insn_format4rcc_method();
					state._fsp--;

					}
					break;
				case 37 :
					// smaliTreeWalker.g:800:5: insn_format51l_type
					{
					pushFollow(FOLLOW_insn_format51l_type_in_instruction2175);
					insn_format51l_type();
					state._fsp--;

					}
					break;
				case 38 :
					// smaliTreeWalker.g:801:5: insn_array_data_directive
					{
					pushFollow(FOLLOW_insn_array_data_directive_in_instruction2181);
					insn_array_data_directive();
					state._fsp--;

					}
					break;
				case 39 :
					// smaliTreeWalker.g:802:5: insn_packed_switch_directive
					{
					pushFollow(FOLLOW_insn_packed_switch_directive_in_instruction2187);
					insn_packed_switch_directive();
					state._fsp--;

					}
					break;
				case 40 :
					// smaliTreeWalker.g:803:5: insn_sparse_switch_directive
					{
					pushFollow(FOLLOW_insn_sparse_switch_directive_in_instruction2193);
					insn_sparse_switch_directive();
					state._fsp--;

					}
					break;

			}
		}
		catch (Exception ex) {

			    reportError(new SemanticException(input, ((CommonTree)retval.start), ex.getMessage()));
			    recover(input, null);
			  
		}

		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction"



	// $ANTLR start "insn_format10t"
	// smaliTreeWalker.g:809:1: insn_format10t : ^( I_STATEMENT_FORMAT10t INSTRUCTION_FORMAT10t label_ref ) ;
	public final void insn_format10t() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT10t114=null;
		Label label_ref115 =null;

		try {
			// smaliTreeWalker.g:810:3: ( ^( I_STATEMENT_FORMAT10t INSTRUCTION_FORMAT10t label_ref ) )
			// smaliTreeWalker.g:811:5: ^( I_STATEMENT_FORMAT10t INSTRUCTION_FORMAT10t label_ref )
			{
			match(input,I_STATEMENT_FORMAT10t,FOLLOW_I_STATEMENT_FORMAT10t_in_insn_format10t2217); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT10t114=(CommonTree)match(input,INSTRUCTION_FORMAT10t,FOLLOW_INSTRUCTION_FORMAT10t_in_insn_format10t2219); 
			pushFollow(FOLLOW_label_ref_in_insn_format10t2221);
			label_ref115=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT10t114!=null?INSTRUCTION_FORMAT10t114.getText():null));
			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction10t(opcode, label_ref115));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format10t"



	// $ANTLR start "insn_format10x"
	// smaliTreeWalker.g:817:1: insn_format10x : ^( I_STATEMENT_FORMAT10x INSTRUCTION_FORMAT10x ) ;
	public final void insn_format10x() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT10x116=null;

		try {
			// smaliTreeWalker.g:818:3: ( ^( I_STATEMENT_FORMAT10x INSTRUCTION_FORMAT10x ) )
			// smaliTreeWalker.g:819:5: ^( I_STATEMENT_FORMAT10x INSTRUCTION_FORMAT10x )
			{
			match(input,I_STATEMENT_FORMAT10x,FOLLOW_I_STATEMENT_FORMAT10x_in_insn_format10x2244); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT10x116=(CommonTree)match(input,INSTRUCTION_FORMAT10x,FOLLOW_INSTRUCTION_FORMAT10x_in_insn_format10x2246); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT10x116!=null?INSTRUCTION_FORMAT10x116.getText():null));
			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction10x(opcode));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format10x"



	// $ANTLR start "insn_format11n"
	// smaliTreeWalker.g:825:1: insn_format11n : ^( I_STATEMENT_FORMAT11n INSTRUCTION_FORMAT11n REGISTER short_integral_literal ) ;
	public final void insn_format11n() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT11n117=null;
		CommonTree REGISTER118=null;
		short short_integral_literal119 =0;

		try {
			// smaliTreeWalker.g:826:3: ( ^( I_STATEMENT_FORMAT11n INSTRUCTION_FORMAT11n REGISTER short_integral_literal ) )
			// smaliTreeWalker.g:827:5: ^( I_STATEMENT_FORMAT11n INSTRUCTION_FORMAT11n REGISTER short_integral_literal )
			{
			match(input,I_STATEMENT_FORMAT11n,FOLLOW_I_STATEMENT_FORMAT11n_in_insn_format11n2269); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT11n117=(CommonTree)match(input,INSTRUCTION_FORMAT11n,FOLLOW_INSTRUCTION_FORMAT11n_in_insn_format11n2271); 
			REGISTER118=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format11n2273); 
			pushFollow(FOLLOW_short_integral_literal_in_insn_format11n2275);
			short_integral_literal119=short_integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT11n117!=null?INSTRUCTION_FORMAT11n117.getText():null));
			      byte regA = parseRegister_nibble((REGISTER118!=null?REGISTER118.getText():null));

			      short litB = short_integral_literal119;
			      LiteralTools.checkNibble(litB);

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction11n(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format11n"



	// $ANTLR start "insn_format11x"
	// smaliTreeWalker.g:838:1: insn_format11x : ^( I_STATEMENT_FORMAT11x INSTRUCTION_FORMAT11x REGISTER ) ;
	public final void insn_format11x() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT11x120=null;
		CommonTree REGISTER121=null;

		try {
			// smaliTreeWalker.g:839:3: ( ^( I_STATEMENT_FORMAT11x INSTRUCTION_FORMAT11x REGISTER ) )
			// smaliTreeWalker.g:840:5: ^( I_STATEMENT_FORMAT11x INSTRUCTION_FORMAT11x REGISTER )
			{
			match(input,I_STATEMENT_FORMAT11x,FOLLOW_I_STATEMENT_FORMAT11x_in_insn_format11x2298); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT11x120=(CommonTree)match(input,INSTRUCTION_FORMAT11x,FOLLOW_INSTRUCTION_FORMAT11x_in_insn_format11x2300); 
			REGISTER121=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format11x2302); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT11x120!=null?INSTRUCTION_FORMAT11x120.getText():null));
			      short regA = parseRegister_byte((REGISTER121!=null?REGISTER121.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction11x(opcode, regA));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format11x"



	// $ANTLR start "insn_format12x"
	// smaliTreeWalker.g:848:1: insn_format12x : ^( I_STATEMENT_FORMAT12x INSTRUCTION_FORMAT12x registerA= REGISTER registerB= REGISTER ) ;
	public final void insn_format12x() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT12x122=null;

		try {
			// smaliTreeWalker.g:849:3: ( ^( I_STATEMENT_FORMAT12x INSTRUCTION_FORMAT12x registerA= REGISTER registerB= REGISTER ) )
			// smaliTreeWalker.g:850:5: ^( I_STATEMENT_FORMAT12x INSTRUCTION_FORMAT12x registerA= REGISTER registerB= REGISTER )
			{
			match(input,I_STATEMENT_FORMAT12x,FOLLOW_I_STATEMENT_FORMAT12x_in_insn_format12x2325); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT12x122=(CommonTree)match(input,INSTRUCTION_FORMAT12x,FOLLOW_INSTRUCTION_FORMAT12x_in_insn_format12x2327); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format12x2331); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format12x2335); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT12x122!=null?INSTRUCTION_FORMAT12x122.getText():null));
			      byte regA = parseRegister_nibble((registerA!=null?registerA.getText():null));
			      byte regB = parseRegister_nibble((registerB!=null?registerB.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction12x(opcode, regA, regB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format12x"



	// $ANTLR start "insn_format20bc"
	// smaliTreeWalker.g:859:1: insn_format20bc : ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc verification_error_type verification_error_reference ) ;
	public final void insn_format20bc() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT20bc123=null;
		int verification_error_type124 =0;
		ImmutableReference verification_error_reference125 =null;

		try {
			// smaliTreeWalker.g:860:3: ( ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc verification_error_type verification_error_reference ) )
			// smaliTreeWalker.g:861:5: ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc verification_error_type verification_error_reference )
			{
			match(input,I_STATEMENT_FORMAT20bc,FOLLOW_I_STATEMENT_FORMAT20bc_in_insn_format20bc2358); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT20bc123=(CommonTree)match(input,INSTRUCTION_FORMAT20bc,FOLLOW_INSTRUCTION_FORMAT20bc_in_insn_format20bc2360); 
			pushFollow(FOLLOW_verification_error_type_in_insn_format20bc2362);
			verification_error_type124=verification_error_type();
			state._fsp--;

			pushFollow(FOLLOW_verification_error_reference_in_insn_format20bc2364);
			verification_error_reference125=verification_error_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT20bc123!=null?INSTRUCTION_FORMAT20bc123.getText():null));

			      int verificationError = verification_error_type124;
			      ImmutableReference referencedItem = verification_error_reference125;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction20bc(opcode, verificationError,
			              dexBuilder.internReference(referencedItem)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format20bc"



	// $ANTLR start "insn_format20t"
	// smaliTreeWalker.g:872:1: insn_format20t : ^( I_STATEMENT_FORMAT20t INSTRUCTION_FORMAT20t label_ref ) ;
	public final void insn_format20t() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT20t126=null;
		Label label_ref127 =null;

		try {
			// smaliTreeWalker.g:873:3: ( ^( I_STATEMENT_FORMAT20t INSTRUCTION_FORMAT20t label_ref ) )
			// smaliTreeWalker.g:874:5: ^( I_STATEMENT_FORMAT20t INSTRUCTION_FORMAT20t label_ref )
			{
			match(input,I_STATEMENT_FORMAT20t,FOLLOW_I_STATEMENT_FORMAT20t_in_insn_format20t2387); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT20t126=(CommonTree)match(input,INSTRUCTION_FORMAT20t,FOLLOW_INSTRUCTION_FORMAT20t_in_insn_format20t2389); 
			pushFollow(FOLLOW_label_ref_in_insn_format20t2391);
			label_ref127=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT20t126!=null?INSTRUCTION_FORMAT20t126.getText():null));
			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction20t(opcode, label_ref127));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format20t"



	// $ANTLR start "insn_format21c_field"
	// smaliTreeWalker.g:880:1: insn_format21c_field : ^( I_STATEMENT_FORMAT21c_FIELD inst= ( INSTRUCTION_FORMAT21c_FIELD | INSTRUCTION_FORMAT21c_FIELD_ODEX ) REGISTER field_reference ) ;
	public final void insn_format21c_field() throws RecognitionException {
		CommonTree inst=null;
		CommonTree REGISTER128=null;
		TreeRuleReturnScope field_reference129 =null;

		try {
			// smaliTreeWalker.g:881:3: ( ^( I_STATEMENT_FORMAT21c_FIELD inst= ( INSTRUCTION_FORMAT21c_FIELD | INSTRUCTION_FORMAT21c_FIELD_ODEX ) REGISTER field_reference ) )
			// smaliTreeWalker.g:882:5: ^( I_STATEMENT_FORMAT21c_FIELD inst= ( INSTRUCTION_FORMAT21c_FIELD | INSTRUCTION_FORMAT21c_FIELD_ODEX ) REGISTER field_reference )
			{
			match(input,I_STATEMENT_FORMAT21c_FIELD,FOLLOW_I_STATEMENT_FORMAT21c_FIELD_in_insn_format21c_field2414); 
			match(input, Token.DOWN, null); 
			inst=(CommonTree)input.LT(1);
			if ( (input.LA(1) >= INSTRUCTION_FORMAT21c_FIELD && input.LA(1) <= INSTRUCTION_FORMAT21c_FIELD_ODEX) ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			REGISTER128=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_field2426); 
			pushFollow(FOLLOW_field_reference_in_insn_format21c_field2428);
			field_reference129=field_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((inst!=null?inst.getText():null));
			      short regA = parseRegister_byte((REGISTER128!=null?REGISTER128.getText():null));

			      ImmutableFieldReference fieldReference = (field_reference129!=null?((smaliTreeWalker.field_reference_return)field_reference129).fieldReference:null);

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21c(opcode, regA,
			              dexBuilder.internFieldReference(fieldReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21c_field"



	// $ANTLR start "insn_format21c_method_handle"
	// smaliTreeWalker.g:893:1: insn_format21c_method_handle : ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE inst= ( INSTRUCTION_FORMAT21c_METHOD_HANDLE ) REGISTER method_handle_reference ) ;
	public final void insn_format21c_method_handle() throws RecognitionException {
		CommonTree inst=null;
		CommonTree REGISTER130=null;
		ImmutableMethodHandleReference method_handle_reference131 =null;

		try {
			// smaliTreeWalker.g:894:3: ( ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE inst= ( INSTRUCTION_FORMAT21c_METHOD_HANDLE ) REGISTER method_handle_reference ) )
			// smaliTreeWalker.g:895:5: ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE inst= ( INSTRUCTION_FORMAT21c_METHOD_HANDLE ) REGISTER method_handle_reference )
			{
			match(input,I_STATEMENT_FORMAT21c_METHOD_HANDLE,FOLLOW_I_STATEMENT_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle2451); 
			match(input, Token.DOWN, null); 
			// smaliTreeWalker.g:895:48: ( INSTRUCTION_FORMAT21c_METHOD_HANDLE )
			// smaliTreeWalker.g:895:49: INSTRUCTION_FORMAT21c_METHOD_HANDLE
			{
			inst=(CommonTree)match(input,INSTRUCTION_FORMAT21c_METHOD_HANDLE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle2456); 
			}

			REGISTER130=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_method_handle2459); 
			pushFollow(FOLLOW_method_handle_reference_in_insn_format21c_method_handle2461);
			method_handle_reference131=method_handle_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((inst!=null?inst.getText():null));
			      short regA = parseRegister_byte((REGISTER130!=null?REGISTER130.getText():null));

			      ImmutableMethodHandleReference methodHandleReference = method_handle_reference131;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21c(opcode, regA,
			              dexBuilder.internMethodHandle(methodHandleReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21c_method_handle"



	// $ANTLR start "insn_format21c_method_type"
	// smaliTreeWalker.g:906:1: insn_format21c_method_type : ^( I_STATEMENT_FORMAT21c_METHOD_TYPE inst= ( INSTRUCTION_FORMAT21c_METHOD_TYPE ) REGISTER method_prototype ) ;
	public final void insn_format21c_method_type() throws RecognitionException {
		CommonTree inst=null;
		CommonTree REGISTER132=null;
		ImmutableMethodProtoReference method_prototype133 =null;

		try {
			// smaliTreeWalker.g:907:3: ( ^( I_STATEMENT_FORMAT21c_METHOD_TYPE inst= ( INSTRUCTION_FORMAT21c_METHOD_TYPE ) REGISTER method_prototype ) )
			// smaliTreeWalker.g:908:5: ^( I_STATEMENT_FORMAT21c_METHOD_TYPE inst= ( INSTRUCTION_FORMAT21c_METHOD_TYPE ) REGISTER method_prototype )
			{
			match(input,I_STATEMENT_FORMAT21c_METHOD_TYPE,FOLLOW_I_STATEMENT_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type2484); 
			match(input, Token.DOWN, null); 
			// smaliTreeWalker.g:908:46: ( INSTRUCTION_FORMAT21c_METHOD_TYPE )
			// smaliTreeWalker.g:908:47: INSTRUCTION_FORMAT21c_METHOD_TYPE
			{
			inst=(CommonTree)match(input,INSTRUCTION_FORMAT21c_METHOD_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type2489); 
			}

			REGISTER132=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_method_type2492); 
			pushFollow(FOLLOW_method_prototype_in_insn_format21c_method_type2494);
			method_prototype133=method_prototype();
			state._fsp--;

			match(input, Token.UP, null); 


			        Opcode opcode = opcodes.getOpcodeByName((inst!=null?inst.getText():null));
			        short regA = parseRegister_byte((REGISTER132!=null?REGISTER132.getText():null));

			        ImmutableMethodProtoReference methodProtoReference = method_prototype133;

			        method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21c(opcode, regA,
			                dexBuilder.internMethodProtoReference(methodProtoReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21c_method_type"



	// $ANTLR start "insn_format21c_string"
	// smaliTreeWalker.g:919:1: insn_format21c_string : ^( I_STATEMENT_FORMAT21c_STRING INSTRUCTION_FORMAT21c_STRING REGISTER string_literal ) ;
	public final void insn_format21c_string() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21c_STRING134=null;
		CommonTree REGISTER135=null;
		String string_literal136 =null;

		try {
			// smaliTreeWalker.g:920:3: ( ^( I_STATEMENT_FORMAT21c_STRING INSTRUCTION_FORMAT21c_STRING REGISTER string_literal ) )
			// smaliTreeWalker.g:921:5: ^( I_STATEMENT_FORMAT21c_STRING INSTRUCTION_FORMAT21c_STRING REGISTER string_literal )
			{
			match(input,I_STATEMENT_FORMAT21c_STRING,FOLLOW_I_STATEMENT_FORMAT21c_STRING_in_insn_format21c_string2517); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21c_STRING134=(CommonTree)match(input,INSTRUCTION_FORMAT21c_STRING,FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_insn_format21c_string2519); 
			REGISTER135=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_string2521); 
			pushFollow(FOLLOW_string_literal_in_insn_format21c_string2523);
			string_literal136=string_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21c_STRING134!=null?INSTRUCTION_FORMAT21c_STRING134.getText():null));
			      short regA = parseRegister_byte((REGISTER135!=null?REGISTER135.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21c(opcode, regA,
			              dexBuilder.internStringReference(string_literal136)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21c_string"



	// $ANTLR start "insn_format21c_type"
	// smaliTreeWalker.g:930:1: insn_format21c_type : ^( I_STATEMENT_FORMAT21c_TYPE INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor ) ;
	public final void insn_format21c_type() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21c_TYPE137=null;
		CommonTree REGISTER138=null;
		TreeRuleReturnScope nonvoid_type_descriptor139 =null;

		try {
			// smaliTreeWalker.g:931:3: ( ^( I_STATEMENT_FORMAT21c_TYPE INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor ) )
			// smaliTreeWalker.g:932:5: ^( I_STATEMENT_FORMAT21c_TYPE INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor )
			{
			match(input,I_STATEMENT_FORMAT21c_TYPE,FOLLOW_I_STATEMENT_FORMAT21c_TYPE_in_insn_format21c_type2546); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21c_TYPE137=(CommonTree)match(input,INSTRUCTION_FORMAT21c_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_insn_format21c_type2548); 
			REGISTER138=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_type2550); 
			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format21c_type2552);
			nonvoid_type_descriptor139=nonvoid_type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21c_TYPE137!=null?INSTRUCTION_FORMAT21c_TYPE137.getText():null));
			      short regA = parseRegister_byte((REGISTER138!=null?REGISTER138.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21c(opcode, regA,
			              dexBuilder.internTypeReference((nonvoid_type_descriptor139!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor139).type:null))));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21c_type"



	// $ANTLR start "insn_format21ih"
	// smaliTreeWalker.g:941:1: insn_format21ih : ^( I_STATEMENT_FORMAT21ih INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal ) ;
	public final void insn_format21ih() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21ih140=null;
		CommonTree REGISTER141=null;
		int fixed_32bit_literal142 =0;

		try {
			// smaliTreeWalker.g:942:3: ( ^( I_STATEMENT_FORMAT21ih INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal ) )
			// smaliTreeWalker.g:943:5: ^( I_STATEMENT_FORMAT21ih INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal )
			{
			match(input,I_STATEMENT_FORMAT21ih,FOLLOW_I_STATEMENT_FORMAT21ih_in_insn_format21ih2575); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21ih140=(CommonTree)match(input,INSTRUCTION_FORMAT21ih,FOLLOW_INSTRUCTION_FORMAT21ih_in_insn_format21ih2577); 
			REGISTER141=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21ih2579); 
			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_format21ih2581);
			fixed_32bit_literal142=fixed_32bit_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21ih140!=null?INSTRUCTION_FORMAT21ih140.getText():null));
			      short regA = parseRegister_byte((REGISTER141!=null?REGISTER141.getText():null));

			      int litB = fixed_32bit_literal142;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21ih(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21ih"



	// $ANTLR start "insn_format21lh"
	// smaliTreeWalker.g:953:1: insn_format21lh : ^( I_STATEMENT_FORMAT21lh INSTRUCTION_FORMAT21lh REGISTER fixed_64bit_literal ) ;
	public final void insn_format21lh() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21lh143=null;
		CommonTree REGISTER144=null;
		long fixed_64bit_literal145 =0;

		try {
			// smaliTreeWalker.g:954:3: ( ^( I_STATEMENT_FORMAT21lh INSTRUCTION_FORMAT21lh REGISTER fixed_64bit_literal ) )
			// smaliTreeWalker.g:955:5: ^( I_STATEMENT_FORMAT21lh INSTRUCTION_FORMAT21lh REGISTER fixed_64bit_literal )
			{
			match(input,I_STATEMENT_FORMAT21lh,FOLLOW_I_STATEMENT_FORMAT21lh_in_insn_format21lh2604); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21lh143=(CommonTree)match(input,INSTRUCTION_FORMAT21lh,FOLLOW_INSTRUCTION_FORMAT21lh_in_insn_format21lh2606); 
			REGISTER144=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21lh2608); 
			pushFollow(FOLLOW_fixed_64bit_literal_in_insn_format21lh2610);
			fixed_64bit_literal145=fixed_64bit_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21lh143!=null?INSTRUCTION_FORMAT21lh143.getText():null));
			      short regA = parseRegister_byte((REGISTER144!=null?REGISTER144.getText():null));

			      long litB = fixed_64bit_literal145;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21lh(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21lh"



	// $ANTLR start "insn_format21s"
	// smaliTreeWalker.g:965:1: insn_format21s : ^( I_STATEMENT_FORMAT21s INSTRUCTION_FORMAT21s REGISTER short_integral_literal ) ;
	public final void insn_format21s() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21s146=null;
		CommonTree REGISTER147=null;
		short short_integral_literal148 =0;

		try {
			// smaliTreeWalker.g:966:3: ( ^( I_STATEMENT_FORMAT21s INSTRUCTION_FORMAT21s REGISTER short_integral_literal ) )
			// smaliTreeWalker.g:967:5: ^( I_STATEMENT_FORMAT21s INSTRUCTION_FORMAT21s REGISTER short_integral_literal )
			{
			match(input,I_STATEMENT_FORMAT21s,FOLLOW_I_STATEMENT_FORMAT21s_in_insn_format21s2633); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21s146=(CommonTree)match(input,INSTRUCTION_FORMAT21s,FOLLOW_INSTRUCTION_FORMAT21s_in_insn_format21s2635); 
			REGISTER147=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21s2637); 
			pushFollow(FOLLOW_short_integral_literal_in_insn_format21s2639);
			short_integral_literal148=short_integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21s146!=null?INSTRUCTION_FORMAT21s146.getText():null));
			      short regA = parseRegister_byte((REGISTER147!=null?REGISTER147.getText():null));

			      short litB = short_integral_literal148;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21s(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21s"



	// $ANTLR start "insn_format21t"
	// smaliTreeWalker.g:977:1: insn_format21t : ^( I_STATEMENT_FORMAT21t INSTRUCTION_FORMAT21t REGISTER label_ref ) ;
	public final void insn_format21t() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT21t149=null;
		CommonTree REGISTER150=null;
		Label label_ref151 =null;

		try {
			// smaliTreeWalker.g:978:3: ( ^( I_STATEMENT_FORMAT21t INSTRUCTION_FORMAT21t REGISTER label_ref ) )
			// smaliTreeWalker.g:979:5: ^( I_STATEMENT_FORMAT21t INSTRUCTION_FORMAT21t REGISTER label_ref )
			{
			match(input,I_STATEMENT_FORMAT21t,FOLLOW_I_STATEMENT_FORMAT21t_in_insn_format21t2662); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT21t149=(CommonTree)match(input,INSTRUCTION_FORMAT21t,FOLLOW_INSTRUCTION_FORMAT21t_in_insn_format21t2664); 
			REGISTER150=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21t2666); 
			pushFollow(FOLLOW_label_ref_in_insn_format21t2668);
			label_ref151=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT21t149!=null?INSTRUCTION_FORMAT21t149.getText():null));
			      short regA = parseRegister_byte((REGISTER150!=null?REGISTER150.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction21t(opcode, regA, label_ref151));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format21t"



	// $ANTLR start "insn_format22b"
	// smaliTreeWalker.g:987:1: insn_format22b : ^( I_STATEMENT_FORMAT22b INSTRUCTION_FORMAT22b registerA= REGISTER registerB= REGISTER short_integral_literal ) ;
	public final void insn_format22b() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT22b152=null;
		short short_integral_literal153 =0;

		try {
			// smaliTreeWalker.g:988:3: ( ^( I_STATEMENT_FORMAT22b INSTRUCTION_FORMAT22b registerA= REGISTER registerB= REGISTER short_integral_literal ) )
			// smaliTreeWalker.g:989:5: ^( I_STATEMENT_FORMAT22b INSTRUCTION_FORMAT22b registerA= REGISTER registerB= REGISTER short_integral_literal )
			{
			match(input,I_STATEMENT_FORMAT22b,FOLLOW_I_STATEMENT_FORMAT22b_in_insn_format22b2691); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT22b152=(CommonTree)match(input,INSTRUCTION_FORMAT22b,FOLLOW_INSTRUCTION_FORMAT22b_in_insn_format22b2693); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22b2697); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22b2701); 
			pushFollow(FOLLOW_short_integral_literal_in_insn_format22b2703);
			short_integral_literal153=short_integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT22b152!=null?INSTRUCTION_FORMAT22b152.getText():null));
			      short regA = parseRegister_byte((registerA!=null?registerA.getText():null));
			      short regB = parseRegister_byte((registerB!=null?registerB.getText():null));

			      short litC = short_integral_literal153;
			      LiteralTools.checkByte(litC);

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22b(opcode, regA, regB, litC));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22b"



	// $ANTLR start "insn_format22c_field"
	// smaliTreeWalker.g:1001:1: insn_format22c_field : ^( I_STATEMENT_FORMAT22c_FIELD inst= ( INSTRUCTION_FORMAT22c_FIELD | INSTRUCTION_FORMAT22c_FIELD_ODEX ) registerA= REGISTER registerB= REGISTER field_reference ) ;
	public final void insn_format22c_field() throws RecognitionException {
		CommonTree inst=null;
		CommonTree registerA=null;
		CommonTree registerB=null;
		TreeRuleReturnScope field_reference154 =null;

		try {
			// smaliTreeWalker.g:1002:3: ( ^( I_STATEMENT_FORMAT22c_FIELD inst= ( INSTRUCTION_FORMAT22c_FIELD | INSTRUCTION_FORMAT22c_FIELD_ODEX ) registerA= REGISTER registerB= REGISTER field_reference ) )
			// smaliTreeWalker.g:1003:5: ^( I_STATEMENT_FORMAT22c_FIELD inst= ( INSTRUCTION_FORMAT22c_FIELD | INSTRUCTION_FORMAT22c_FIELD_ODEX ) registerA= REGISTER registerB= REGISTER field_reference )
			{
			match(input,I_STATEMENT_FORMAT22c_FIELD,FOLLOW_I_STATEMENT_FORMAT22c_FIELD_in_insn_format22c_field2726); 
			match(input, Token.DOWN, null); 
			inst=(CommonTree)input.LT(1);
			if ( (input.LA(1) >= INSTRUCTION_FORMAT22c_FIELD && input.LA(1) <= INSTRUCTION_FORMAT22c_FIELD_ODEX) ) {
				input.consume();
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field2740); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field2744); 
			pushFollow(FOLLOW_field_reference_in_insn_format22c_field2746);
			field_reference154=field_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((inst!=null?inst.getText():null));
			      byte regA = parseRegister_nibble((registerA!=null?registerA.getText():null));
			      byte regB = parseRegister_nibble((registerB!=null?registerB.getText():null));

			      ImmutableFieldReference fieldReference = (field_reference154!=null?((smaliTreeWalker.field_reference_return)field_reference154).fieldReference:null);

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22c(opcode, regA, regB,
			              dexBuilder.internFieldReference(fieldReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22c_field"



	// $ANTLR start "insn_format22c_type"
	// smaliTreeWalker.g:1015:1: insn_format22c_type : ^( I_STATEMENT_FORMAT22c_TYPE INSTRUCTION_FORMAT22c_TYPE registerA= REGISTER registerB= REGISTER nonvoid_type_descriptor ) ;
	public final void insn_format22c_type() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT22c_TYPE155=null;
		TreeRuleReturnScope nonvoid_type_descriptor156 =null;

		try {
			// smaliTreeWalker.g:1016:3: ( ^( I_STATEMENT_FORMAT22c_TYPE INSTRUCTION_FORMAT22c_TYPE registerA= REGISTER registerB= REGISTER nonvoid_type_descriptor ) )
			// smaliTreeWalker.g:1017:5: ^( I_STATEMENT_FORMAT22c_TYPE INSTRUCTION_FORMAT22c_TYPE registerA= REGISTER registerB= REGISTER nonvoid_type_descriptor )
			{
			match(input,I_STATEMENT_FORMAT22c_TYPE,FOLLOW_I_STATEMENT_FORMAT22c_TYPE_in_insn_format22c_type2769); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT22c_TYPE155=(CommonTree)match(input,INSTRUCTION_FORMAT22c_TYPE,FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_insn_format22c_type2771); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_type2775); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_type2779); 
			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format22c_type2781);
			nonvoid_type_descriptor156=nonvoid_type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT22c_TYPE155!=null?INSTRUCTION_FORMAT22c_TYPE155.getText():null));
			      byte regA = parseRegister_nibble((registerA!=null?registerA.getText():null));
			      byte regB = parseRegister_nibble((registerB!=null?registerB.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22c(opcode, regA, regB,
			              dexBuilder.internTypeReference((nonvoid_type_descriptor156!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor156).type:null))));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22c_type"



	// $ANTLR start "insn_format22s"
	// smaliTreeWalker.g:1027:1: insn_format22s : ^( I_STATEMENT_FORMAT22s INSTRUCTION_FORMAT22s registerA= REGISTER registerB= REGISTER short_integral_literal ) ;
	public final void insn_format22s() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT22s157=null;
		short short_integral_literal158 =0;

		try {
			// smaliTreeWalker.g:1028:3: ( ^( I_STATEMENT_FORMAT22s INSTRUCTION_FORMAT22s registerA= REGISTER registerB= REGISTER short_integral_literal ) )
			// smaliTreeWalker.g:1029:5: ^( I_STATEMENT_FORMAT22s INSTRUCTION_FORMAT22s registerA= REGISTER registerB= REGISTER short_integral_literal )
			{
			match(input,I_STATEMENT_FORMAT22s,FOLLOW_I_STATEMENT_FORMAT22s_in_insn_format22s2804); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT22s157=(CommonTree)match(input,INSTRUCTION_FORMAT22s,FOLLOW_INSTRUCTION_FORMAT22s_in_insn_format22s2806); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22s2810); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22s2814); 
			pushFollow(FOLLOW_short_integral_literal_in_insn_format22s2816);
			short_integral_literal158=short_integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT22s157!=null?INSTRUCTION_FORMAT22s157.getText():null));
			      byte regA = parseRegister_nibble((registerA!=null?registerA.getText():null));
			      byte regB = parseRegister_nibble((registerB!=null?registerB.getText():null));

			      short litC = short_integral_literal158;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22s(opcode, regA, regB, litC));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22s"



	// $ANTLR start "insn_format22t"
	// smaliTreeWalker.g:1040:1: insn_format22t : ^( I_STATEMENT_FORMAT22t INSTRUCTION_FORMAT22t registerA= REGISTER registerB= REGISTER label_ref ) ;
	public final void insn_format22t() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT22t159=null;
		Label label_ref160 =null;

		try {
			// smaliTreeWalker.g:1041:3: ( ^( I_STATEMENT_FORMAT22t INSTRUCTION_FORMAT22t registerA= REGISTER registerB= REGISTER label_ref ) )
			// smaliTreeWalker.g:1042:5: ^( I_STATEMENT_FORMAT22t INSTRUCTION_FORMAT22t registerA= REGISTER registerB= REGISTER label_ref )
			{
			match(input,I_STATEMENT_FORMAT22t,FOLLOW_I_STATEMENT_FORMAT22t_in_insn_format22t2839); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT22t159=(CommonTree)match(input,INSTRUCTION_FORMAT22t,FOLLOW_INSTRUCTION_FORMAT22t_in_insn_format22t2841); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22t2845); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22t2849); 
			pushFollow(FOLLOW_label_ref_in_insn_format22t2851);
			label_ref160=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT22t159!=null?INSTRUCTION_FORMAT22t159.getText():null));
			      byte regA = parseRegister_nibble((registerA!=null?registerA.getText():null));
			      byte regB = parseRegister_nibble((registerB!=null?registerB.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22t(opcode, regA, regB, label_ref160));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22t"



	// $ANTLR start "insn_format22x"
	// smaliTreeWalker.g:1051:1: insn_format22x : ^( I_STATEMENT_FORMAT22x INSTRUCTION_FORMAT22x registerA= REGISTER registerB= REGISTER ) ;
	public final void insn_format22x() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT22x161=null;

		try {
			// smaliTreeWalker.g:1052:3: ( ^( I_STATEMENT_FORMAT22x INSTRUCTION_FORMAT22x registerA= REGISTER registerB= REGISTER ) )
			// smaliTreeWalker.g:1053:5: ^( I_STATEMENT_FORMAT22x INSTRUCTION_FORMAT22x registerA= REGISTER registerB= REGISTER )
			{
			match(input,I_STATEMENT_FORMAT22x,FOLLOW_I_STATEMENT_FORMAT22x_in_insn_format22x2874); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT22x161=(CommonTree)match(input,INSTRUCTION_FORMAT22x,FOLLOW_INSTRUCTION_FORMAT22x_in_insn_format22x2876); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22x2880); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22x2884); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT22x161!=null?INSTRUCTION_FORMAT22x161.getText():null));
			      short regA = parseRegister_byte((registerA!=null?registerA.getText():null));
			      int regB = parseRegister_short((registerB!=null?registerB.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction22x(opcode, regA, regB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format22x"



	// $ANTLR start "insn_format23x"
	// smaliTreeWalker.g:1062:1: insn_format23x : ^( I_STATEMENT_FORMAT23x INSTRUCTION_FORMAT23x registerA= REGISTER registerB= REGISTER registerC= REGISTER ) ;
	public final void insn_format23x() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree registerC=null;
		CommonTree INSTRUCTION_FORMAT23x162=null;

		try {
			// smaliTreeWalker.g:1063:3: ( ^( I_STATEMENT_FORMAT23x INSTRUCTION_FORMAT23x registerA= REGISTER registerB= REGISTER registerC= REGISTER ) )
			// smaliTreeWalker.g:1064:5: ^( I_STATEMENT_FORMAT23x INSTRUCTION_FORMAT23x registerA= REGISTER registerB= REGISTER registerC= REGISTER )
			{
			match(input,I_STATEMENT_FORMAT23x,FOLLOW_I_STATEMENT_FORMAT23x_in_insn_format23x2907); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT23x162=(CommonTree)match(input,INSTRUCTION_FORMAT23x,FOLLOW_INSTRUCTION_FORMAT23x_in_insn_format23x2909); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x2913); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x2917); 
			registerC=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x2921); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT23x162!=null?INSTRUCTION_FORMAT23x162.getText():null));
			      short regA = parseRegister_byte((registerA!=null?registerA.getText():null));
			      short regB = parseRegister_byte((registerB!=null?registerB.getText():null));
			      short regC = parseRegister_byte((registerC!=null?registerC.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction23x(opcode, regA, regB, regC));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format23x"



	// $ANTLR start "insn_format30t"
	// smaliTreeWalker.g:1074:1: insn_format30t : ^( I_STATEMENT_FORMAT30t INSTRUCTION_FORMAT30t label_ref ) ;
	public final void insn_format30t() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT30t163=null;
		Label label_ref164 =null;

		try {
			// smaliTreeWalker.g:1075:3: ( ^( I_STATEMENT_FORMAT30t INSTRUCTION_FORMAT30t label_ref ) )
			// smaliTreeWalker.g:1076:5: ^( I_STATEMENT_FORMAT30t INSTRUCTION_FORMAT30t label_ref )
			{
			match(input,I_STATEMENT_FORMAT30t,FOLLOW_I_STATEMENT_FORMAT30t_in_insn_format30t2944); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT30t163=(CommonTree)match(input,INSTRUCTION_FORMAT30t,FOLLOW_INSTRUCTION_FORMAT30t_in_insn_format30t2946); 
			pushFollow(FOLLOW_label_ref_in_insn_format30t2948);
			label_ref164=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT30t163!=null?INSTRUCTION_FORMAT30t163.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction30t(opcode, label_ref164));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format30t"



	// $ANTLR start "insn_format31c"
	// smaliTreeWalker.g:1083:1: insn_format31c : ^( I_STATEMENT_FORMAT31c INSTRUCTION_FORMAT31c REGISTER string_literal ) ;
	public final void insn_format31c() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT31c165=null;
		CommonTree REGISTER166=null;
		String string_literal167 =null;

		try {
			// smaliTreeWalker.g:1084:3: ( ^( I_STATEMENT_FORMAT31c INSTRUCTION_FORMAT31c REGISTER string_literal ) )
			// smaliTreeWalker.g:1085:5: ^( I_STATEMENT_FORMAT31c INSTRUCTION_FORMAT31c REGISTER string_literal )
			{
			match(input,I_STATEMENT_FORMAT31c,FOLLOW_I_STATEMENT_FORMAT31c_in_insn_format31c2971); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT31c165=(CommonTree)match(input,INSTRUCTION_FORMAT31c,FOLLOW_INSTRUCTION_FORMAT31c_in_insn_format31c2973); 
			REGISTER166=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31c2975); 
			pushFollow(FOLLOW_string_literal_in_insn_format31c2977);
			string_literal167=string_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT31c165!=null?INSTRUCTION_FORMAT31c165.getText():null));
			      short regA = parseRegister_byte((REGISTER166!=null?REGISTER166.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction31c(opcode, regA,
			              dexBuilder.internStringReference(string_literal167)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format31c"



	// $ANTLR start "insn_format31i"
	// smaliTreeWalker.g:1094:1: insn_format31i : ^( I_STATEMENT_FORMAT31i INSTRUCTION_FORMAT31i REGISTER fixed_32bit_literal ) ;
	public final void insn_format31i() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT31i168=null;
		CommonTree REGISTER169=null;
		int fixed_32bit_literal170 =0;

		try {
			// smaliTreeWalker.g:1095:3: ( ^( I_STATEMENT_FORMAT31i INSTRUCTION_FORMAT31i REGISTER fixed_32bit_literal ) )
			// smaliTreeWalker.g:1096:5: ^( I_STATEMENT_FORMAT31i INSTRUCTION_FORMAT31i REGISTER fixed_32bit_literal )
			{
			match(input,I_STATEMENT_FORMAT31i,FOLLOW_I_STATEMENT_FORMAT31i_in_insn_format31i3000); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT31i168=(CommonTree)match(input,INSTRUCTION_FORMAT31i,FOLLOW_INSTRUCTION_FORMAT31i_in_insn_format31i3002); 
			REGISTER169=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31i3004); 
			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_format31i3006);
			fixed_32bit_literal170=fixed_32bit_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT31i168!=null?INSTRUCTION_FORMAT31i168.getText():null));
			      short regA = parseRegister_byte((REGISTER169!=null?REGISTER169.getText():null));

			      int litB = fixed_32bit_literal170;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction31i(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format31i"



	// $ANTLR start "insn_format31t"
	// smaliTreeWalker.g:1106:1: insn_format31t : ^( I_STATEMENT_FORMAT31t INSTRUCTION_FORMAT31t REGISTER label_ref ) ;
	public final void insn_format31t() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT31t171=null;
		CommonTree REGISTER172=null;
		Label label_ref173 =null;

		try {
			// smaliTreeWalker.g:1107:3: ( ^( I_STATEMENT_FORMAT31t INSTRUCTION_FORMAT31t REGISTER label_ref ) )
			// smaliTreeWalker.g:1108:5: ^( I_STATEMENT_FORMAT31t INSTRUCTION_FORMAT31t REGISTER label_ref )
			{
			match(input,I_STATEMENT_FORMAT31t,FOLLOW_I_STATEMENT_FORMAT31t_in_insn_format31t3029); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT31t171=(CommonTree)match(input,INSTRUCTION_FORMAT31t,FOLLOW_INSTRUCTION_FORMAT31t_in_insn_format31t3031); 
			REGISTER172=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31t3033); 
			pushFollow(FOLLOW_label_ref_in_insn_format31t3035);
			label_ref173=label_ref();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT31t171!=null?INSTRUCTION_FORMAT31t171.getText():null));

			      short regA = parseRegister_byte((REGISTER172!=null?REGISTER172.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction31t(opcode, regA, label_ref173));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format31t"



	// $ANTLR start "insn_format32x"
	// smaliTreeWalker.g:1117:1: insn_format32x : ^( I_STATEMENT_FORMAT32x INSTRUCTION_FORMAT32x registerA= REGISTER registerB= REGISTER ) ;
	public final void insn_format32x() throws RecognitionException {
		CommonTree registerA=null;
		CommonTree registerB=null;
		CommonTree INSTRUCTION_FORMAT32x174=null;

		try {
			// smaliTreeWalker.g:1118:3: ( ^( I_STATEMENT_FORMAT32x INSTRUCTION_FORMAT32x registerA= REGISTER registerB= REGISTER ) )
			// smaliTreeWalker.g:1119:5: ^( I_STATEMENT_FORMAT32x INSTRUCTION_FORMAT32x registerA= REGISTER registerB= REGISTER )
			{
			match(input,I_STATEMENT_FORMAT32x,FOLLOW_I_STATEMENT_FORMAT32x_in_insn_format32x3058); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT32x174=(CommonTree)match(input,INSTRUCTION_FORMAT32x,FOLLOW_INSTRUCTION_FORMAT32x_in_insn_format32x3060); 
			registerA=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format32x3064); 
			registerB=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format32x3068); 
			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT32x174!=null?INSTRUCTION_FORMAT32x174.getText():null));
			      int regA = parseRegister_short((registerA!=null?registerA.getText():null));
			      int regB = parseRegister_short((registerB!=null?registerB.getText():null));

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction32x(opcode, regA, regB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format32x"



	// $ANTLR start "insn_format35c_call_site"
	// smaliTreeWalker.g:1128:1: insn_format35c_call_site : ^( I_STATEMENT_FORMAT35c_CALL_SITE INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference ) ;
	public final void insn_format35c_call_site() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT35c_CALL_SITE175=null;
		TreeRuleReturnScope register_list176 =null;
		ImmutableCallSiteReference call_site_reference177 =null;

		try {
			// smaliTreeWalker.g:1129:3: ( ^( I_STATEMENT_FORMAT35c_CALL_SITE INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference ) )
			// smaliTreeWalker.g:1131:5: ^( I_STATEMENT_FORMAT35c_CALL_SITE INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference )
			{
			match(input,I_STATEMENT_FORMAT35c_CALL_SITE,FOLLOW_I_STATEMENT_FORMAT35c_CALL_SITE_in_insn_format35c_call_site3096); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT35c_CALL_SITE175=(CommonTree)match(input,INSTRUCTION_FORMAT35c_CALL_SITE,FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_insn_format35c_call_site3098); 
			pushFollow(FOLLOW_register_list_in_insn_format35c_call_site3100);
			register_list176=register_list();
			state._fsp--;

			pushFollow(FOLLOW_call_site_reference_in_insn_format35c_call_site3102);
			call_site_reference177=call_site_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			        Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT35c_CALL_SITE175!=null?INSTRUCTION_FORMAT35c_CALL_SITE175.getText():null));

			        //this depends on the fact that register_list returns a byte[5]
			        byte[] registers = (register_list176!=null?((smaliTreeWalker.register_list_return)register_list176).registers:null);
			        byte registerCount = (register_list176!=null?((smaliTreeWalker.register_list_return)register_list176).registerCount:0);

			        ImmutableCallSiteReference callSiteReference = call_site_reference177;

			        method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction35c(opcode, registerCount, registers[0],
			                registers[1], registers[2], registers[3], registers[4], dexBuilder.internCallSite(callSiteReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format35c_call_site"



	// $ANTLR start "insn_format35c_method"
	// smaliTreeWalker.g:1145:1: insn_format35c_method : ^( I_STATEMENT_FORMAT35c_METHOD INSTRUCTION_FORMAT35c_METHOD register_list method_reference ) ;
	public final void insn_format35c_method() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT35c_METHOD178=null;
		TreeRuleReturnScope register_list179 =null;
		ImmutableMethodReference method_reference180 =null;

		try {
			// smaliTreeWalker.g:1146:3: ( ^( I_STATEMENT_FORMAT35c_METHOD INSTRUCTION_FORMAT35c_METHOD register_list method_reference ) )
			// smaliTreeWalker.g:1147:5: ^( I_STATEMENT_FORMAT35c_METHOD INSTRUCTION_FORMAT35c_METHOD register_list method_reference )
			{
			match(input,I_STATEMENT_FORMAT35c_METHOD,FOLLOW_I_STATEMENT_FORMAT35c_METHOD_in_insn_format35c_method3125); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT35c_METHOD178=(CommonTree)match(input,INSTRUCTION_FORMAT35c_METHOD,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_insn_format35c_method3127); 
			pushFollow(FOLLOW_register_list_in_insn_format35c_method3129);
			register_list179=register_list();
			state._fsp--;

			pushFollow(FOLLOW_method_reference_in_insn_format35c_method3131);
			method_reference180=method_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT35c_METHOD178!=null?INSTRUCTION_FORMAT35c_METHOD178.getText():null));

			      //this depends on the fact that register_list returns a byte[5]
			      byte[] registers = (register_list179!=null?((smaliTreeWalker.register_list_return)register_list179).registers:null);
			      byte registerCount = (register_list179!=null?((smaliTreeWalker.register_list_return)register_list179).registerCount:0);

			      ImmutableMethodReference methodReference = method_reference180;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction35c(opcode, registerCount, registers[0], registers[1],
			              registers[2], registers[3], registers[4], dexBuilder.internMethodReference(methodReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format35c_method"



	// $ANTLR start "insn_format35c_type"
	// smaliTreeWalker.g:1161:1: insn_format35c_type : ^( I_STATEMENT_FORMAT35c_TYPE INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor ) ;
	public final void insn_format35c_type() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT35c_TYPE181=null;
		TreeRuleReturnScope register_list182 =null;
		TreeRuleReturnScope nonvoid_type_descriptor183 =null;

		try {
			// smaliTreeWalker.g:1162:3: ( ^( I_STATEMENT_FORMAT35c_TYPE INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor ) )
			// smaliTreeWalker.g:1163:5: ^( I_STATEMENT_FORMAT35c_TYPE INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor )
			{
			match(input,I_STATEMENT_FORMAT35c_TYPE,FOLLOW_I_STATEMENT_FORMAT35c_TYPE_in_insn_format35c_type3154); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT35c_TYPE181=(CommonTree)match(input,INSTRUCTION_FORMAT35c_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_insn_format35c_type3156); 
			pushFollow(FOLLOW_register_list_in_insn_format35c_type3158);
			register_list182=register_list();
			state._fsp--;

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format35c_type3160);
			nonvoid_type_descriptor183=nonvoid_type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT35c_TYPE181!=null?INSTRUCTION_FORMAT35c_TYPE181.getText():null));

			      //this depends on the fact that register_list returns a byte[5]
			      byte[] registers = (register_list182!=null?((smaliTreeWalker.register_list_return)register_list182).registers:null);
			      byte registerCount = (register_list182!=null?((smaliTreeWalker.register_list_return)register_list182).registerCount:0);

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction35c(opcode, registerCount, registers[0], registers[1],
			              registers[2], registers[3], registers[4], dexBuilder.internTypeReference((nonvoid_type_descriptor183!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor183).type:null))));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format35c_type"



	// $ANTLR start "insn_format3rc_call_site"
	// smaliTreeWalker.g:1175:1: insn_format3rc_call_site : ^( I_STATEMENT_FORMAT3rc_CALL_SITE INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference ) ;
	public final void insn_format3rc_call_site() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT3rc_CALL_SITE184=null;
		TreeRuleReturnScope register_range185 =null;
		ImmutableCallSiteReference call_site_reference186 =null;

		try {
			// smaliTreeWalker.g:1176:3: ( ^( I_STATEMENT_FORMAT3rc_CALL_SITE INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference ) )
			// smaliTreeWalker.g:1178:5: ^( I_STATEMENT_FORMAT3rc_CALL_SITE INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference )
			{
			match(input,I_STATEMENT_FORMAT3rc_CALL_SITE,FOLLOW_I_STATEMENT_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site3188); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT3rc_CALL_SITE184=(CommonTree)match(input,INSTRUCTION_FORMAT3rc_CALL_SITE,FOLLOW_INSTRUCTION_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site3190); 
			pushFollow(FOLLOW_register_range_in_insn_format3rc_call_site3192);
			register_range185=register_range();
			state._fsp--;

			pushFollow(FOLLOW_call_site_reference_in_insn_format3rc_call_site3194);
			call_site_reference186=call_site_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			        Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT3rc_CALL_SITE184!=null?INSTRUCTION_FORMAT3rc_CALL_SITE184.getText():null));
			        int startRegister = (register_range185!=null?((smaliTreeWalker.register_range_return)register_range185).startRegister:0);
			        int endRegister = (register_range185!=null?((smaliTreeWalker.register_range_return)register_range185).endRegister:0);

			        int registerCount = endRegister - startRegister + 1;

			        ImmutableCallSiteReference callSiteReference = call_site_reference186;

			        method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction3rc(opcode, startRegister, registerCount,
			                dexBuilder.internCallSite(callSiteReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format3rc_call_site"



	// $ANTLR start "insn_format3rc_method"
	// smaliTreeWalker.g:1192:1: insn_format3rc_method : ^( I_STATEMENT_FORMAT3rc_METHOD INSTRUCTION_FORMAT3rc_METHOD register_range method_reference ) ;
	public final void insn_format3rc_method() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT3rc_METHOD187=null;
		TreeRuleReturnScope register_range188 =null;
		ImmutableMethodReference method_reference189 =null;

		try {
			// smaliTreeWalker.g:1193:3: ( ^( I_STATEMENT_FORMAT3rc_METHOD INSTRUCTION_FORMAT3rc_METHOD register_range method_reference ) )
			// smaliTreeWalker.g:1194:5: ^( I_STATEMENT_FORMAT3rc_METHOD INSTRUCTION_FORMAT3rc_METHOD register_range method_reference )
			{
			match(input,I_STATEMENT_FORMAT3rc_METHOD,FOLLOW_I_STATEMENT_FORMAT3rc_METHOD_in_insn_format3rc_method3217); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT3rc_METHOD187=(CommonTree)match(input,INSTRUCTION_FORMAT3rc_METHOD,FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_in_insn_format3rc_method3219); 
			pushFollow(FOLLOW_register_range_in_insn_format3rc_method3221);
			register_range188=register_range();
			state._fsp--;

			pushFollow(FOLLOW_method_reference_in_insn_format3rc_method3223);
			method_reference189=method_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT3rc_METHOD187!=null?INSTRUCTION_FORMAT3rc_METHOD187.getText():null));
			      int startRegister = (register_range188!=null?((smaliTreeWalker.register_range_return)register_range188).startRegister:0);
			      int endRegister = (register_range188!=null?((smaliTreeWalker.register_range_return)register_range188).endRegister:0);

			      int registerCount = endRegister-startRegister+1;

			      ImmutableMethodReference methodReference = method_reference189;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction3rc(opcode, startRegister, registerCount,
			              dexBuilder.internMethodReference(methodReference)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format3rc_method"



	// $ANTLR start "insn_format3rc_type"
	// smaliTreeWalker.g:1208:1: insn_format3rc_type : ^( I_STATEMENT_FORMAT3rc_TYPE INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor ) ;
	public final void insn_format3rc_type() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT3rc_TYPE190=null;
		TreeRuleReturnScope register_range191 =null;
		TreeRuleReturnScope nonvoid_type_descriptor192 =null;

		try {
			// smaliTreeWalker.g:1209:3: ( ^( I_STATEMENT_FORMAT3rc_TYPE INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor ) )
			// smaliTreeWalker.g:1210:5: ^( I_STATEMENT_FORMAT3rc_TYPE INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor )
			{
			match(input,I_STATEMENT_FORMAT3rc_TYPE,FOLLOW_I_STATEMENT_FORMAT3rc_TYPE_in_insn_format3rc_type3246); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT3rc_TYPE190=(CommonTree)match(input,INSTRUCTION_FORMAT3rc_TYPE,FOLLOW_INSTRUCTION_FORMAT3rc_TYPE_in_insn_format3rc_type3248); 
			pushFollow(FOLLOW_register_range_in_insn_format3rc_type3250);
			register_range191=register_range();
			state._fsp--;

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format3rc_type3252);
			nonvoid_type_descriptor192=nonvoid_type_descriptor();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT3rc_TYPE190!=null?INSTRUCTION_FORMAT3rc_TYPE190.getText():null));
			      int startRegister = (register_range191!=null?((smaliTreeWalker.register_range_return)register_range191).startRegister:0);
			      int endRegister = (register_range191!=null?((smaliTreeWalker.register_range_return)register_range191).endRegister:0);

			      int registerCount = endRegister-startRegister+1;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction3rc(opcode, startRegister, registerCount,
			              dexBuilder.internTypeReference((nonvoid_type_descriptor192!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor192).type:null))));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format3rc_type"



	// $ANTLR start "insn_format45cc_method"
	// smaliTreeWalker.g:1222:1: insn_format45cc_method : ^( I_STATEMENT_FORMAT45cc_METHOD INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype ) ;
	public final void insn_format45cc_method() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT45cc_METHOD193=null;
		TreeRuleReturnScope register_list194 =null;
		ImmutableMethodReference method_reference195 =null;
		ImmutableMethodProtoReference method_prototype196 =null;

		try {
			// smaliTreeWalker.g:1223:3: ( ^( I_STATEMENT_FORMAT45cc_METHOD INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype ) )
			// smaliTreeWalker.g:1224:5: ^( I_STATEMENT_FORMAT45cc_METHOD INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype )
			{
			match(input,I_STATEMENT_FORMAT45cc_METHOD,FOLLOW_I_STATEMENT_FORMAT45cc_METHOD_in_insn_format45cc_method3275); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT45cc_METHOD193=(CommonTree)match(input,INSTRUCTION_FORMAT45cc_METHOD,FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_insn_format45cc_method3277); 
			pushFollow(FOLLOW_register_list_in_insn_format45cc_method3279);
			register_list194=register_list();
			state._fsp--;

			pushFollow(FOLLOW_method_reference_in_insn_format45cc_method3281);
			method_reference195=method_reference();
			state._fsp--;

			pushFollow(FOLLOW_method_prototype_in_insn_format45cc_method3283);
			method_prototype196=method_prototype();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT45cc_METHOD193!=null?INSTRUCTION_FORMAT45cc_METHOD193.getText():null));

			      //this depends on the fact that register_list returns a byte[5]
			      byte[] registers = (register_list194!=null?((smaliTreeWalker.register_list_return)register_list194).registers:null);
			      byte registerCount = (register_list194!=null?((smaliTreeWalker.register_list_return)register_list194).registerCount:0);

			      ImmutableMethodReference methodReference = method_reference195;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction45cc(opcode, registerCount, registers[0], registers[1],
			              registers[2], registers[3], registers[4],
			              dexBuilder.internMethodReference(methodReference),
			              dexBuilder.internMethodProtoReference(method_prototype196)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format45cc_method"



	// $ANTLR start "insn_format4rcc_method"
	// smaliTreeWalker.g:1240:1: insn_format4rcc_method : ^( I_STATEMENT_FORMAT4rcc_METHOD INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype ) ;
	public final void insn_format4rcc_method() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT4rcc_METHOD197=null;
		TreeRuleReturnScope register_range198 =null;
		ImmutableMethodReference method_reference199 =null;
		ImmutableMethodProtoReference method_prototype200 =null;

		try {
			// smaliTreeWalker.g:1241:3: ( ^( I_STATEMENT_FORMAT4rcc_METHOD INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype ) )
			// smaliTreeWalker.g:1242:5: ^( I_STATEMENT_FORMAT4rcc_METHOD INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype )
			{
			match(input,I_STATEMENT_FORMAT4rcc_METHOD,FOLLOW_I_STATEMENT_FORMAT4rcc_METHOD_in_insn_format4rcc_method3306); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT4rcc_METHOD197=(CommonTree)match(input,INSTRUCTION_FORMAT4rcc_METHOD,FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_insn_format4rcc_method3308); 
			pushFollow(FOLLOW_register_range_in_insn_format4rcc_method3310);
			register_range198=register_range();
			state._fsp--;

			pushFollow(FOLLOW_method_reference_in_insn_format4rcc_method3312);
			method_reference199=method_reference();
			state._fsp--;

			pushFollow(FOLLOW_method_prototype_in_insn_format4rcc_method3314);
			method_prototype200=method_prototype();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT4rcc_METHOD197!=null?INSTRUCTION_FORMAT4rcc_METHOD197.getText():null));
			      int startRegister = (register_range198!=null?((smaliTreeWalker.register_range_return)register_range198).startRegister:0);
			      int endRegister = (register_range198!=null?((smaliTreeWalker.register_range_return)register_range198).endRegister:0);

			      int registerCount = endRegister-startRegister+1;

			      ImmutableMethodReference methodReference = method_reference199;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction4rcc(opcode, startRegister, registerCount,
			              dexBuilder.internMethodReference(methodReference),
			              dexBuilder.internMethodProtoReference(method_prototype200)));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format4rcc_method"



	// $ANTLR start "insn_format51l_type"
	// smaliTreeWalker.g:1257:1: insn_format51l_type : ^( I_STATEMENT_FORMAT51l INSTRUCTION_FORMAT51l REGISTER fixed_64bit_literal ) ;
	public final void insn_format51l_type() throws RecognitionException {
		CommonTree INSTRUCTION_FORMAT51l201=null;
		CommonTree REGISTER202=null;
		long fixed_64bit_literal203 =0;

		try {
			// smaliTreeWalker.g:1258:3: ( ^( I_STATEMENT_FORMAT51l INSTRUCTION_FORMAT51l REGISTER fixed_64bit_literal ) )
			// smaliTreeWalker.g:1259:5: ^( I_STATEMENT_FORMAT51l INSTRUCTION_FORMAT51l REGISTER fixed_64bit_literal )
			{
			match(input,I_STATEMENT_FORMAT51l,FOLLOW_I_STATEMENT_FORMAT51l_in_insn_format51l_type3337); 
			match(input, Token.DOWN, null); 
			INSTRUCTION_FORMAT51l201=(CommonTree)match(input,INSTRUCTION_FORMAT51l,FOLLOW_INSTRUCTION_FORMAT51l_in_insn_format51l_type3339); 
			REGISTER202=(CommonTree)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format51l_type3341); 
			pushFollow(FOLLOW_fixed_64bit_literal_in_insn_format51l_type3343);
			fixed_64bit_literal203=fixed_64bit_literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      Opcode opcode = opcodes.getOpcodeByName((INSTRUCTION_FORMAT51l201!=null?INSTRUCTION_FORMAT51l201.getText():null));
			      short regA = parseRegister_byte((REGISTER202!=null?REGISTER202.getText():null));

			      long litB = fixed_64bit_literal203;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderInstruction51l(opcode, regA, litB));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_format51l_type"



	// $ANTLR start "insn_array_data_directive"
	// smaliTreeWalker.g:1269:1: insn_array_data_directive : ^( I_STATEMENT_ARRAY_DATA ^( I_ARRAY_ELEMENT_SIZE short_integral_literal ) array_elements ) ;
	public final void insn_array_data_directive() throws RecognitionException {
		short short_integral_literal204 =0;
		List<Number> array_elements205 =null;

		try {
			// smaliTreeWalker.g:1270:3: ( ^( I_STATEMENT_ARRAY_DATA ^( I_ARRAY_ELEMENT_SIZE short_integral_literal ) array_elements ) )
			// smaliTreeWalker.g:1271:5: ^( I_STATEMENT_ARRAY_DATA ^( I_ARRAY_ELEMENT_SIZE short_integral_literal ) array_elements )
			{
			match(input,I_STATEMENT_ARRAY_DATA,FOLLOW_I_STATEMENT_ARRAY_DATA_in_insn_array_data_directive3366); 
			match(input, Token.DOWN, null); 
			match(input,I_ARRAY_ELEMENT_SIZE,FOLLOW_I_ARRAY_ELEMENT_SIZE_in_insn_array_data_directive3369); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_short_integral_literal_in_insn_array_data_directive3371);
			short_integral_literal204=short_integral_literal();
			state._fsp--;

			match(input, Token.UP, null); 

			pushFollow(FOLLOW_array_elements_in_insn_array_data_directive3374);
			array_elements205=array_elements();
			state._fsp--;

			match(input, Token.UP, null); 


			      int elementWidth = short_integral_literal204;
			      List<Number> elements = array_elements205;

			      method_stack.peek().methodBuilder.addInstruction(new BuilderArrayPayload(elementWidth, array_elements205));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_array_data_directive"



	// $ANTLR start "insn_packed_switch_directive"
	// smaliTreeWalker.g:1279:1: insn_packed_switch_directive : ^( I_STATEMENT_PACKED_SWITCH ^( I_PACKED_SWITCH_START_KEY fixed_32bit_literal ) packed_switch_elements ) ;
	public final void insn_packed_switch_directive() throws RecognitionException {
		int fixed_32bit_literal206 =0;
		List<Label> packed_switch_elements207 =null;

		try {
			// smaliTreeWalker.g:1280:3: ( ^( I_STATEMENT_PACKED_SWITCH ^( I_PACKED_SWITCH_START_KEY fixed_32bit_literal ) packed_switch_elements ) )
			// smaliTreeWalker.g:1281:5: ^( I_STATEMENT_PACKED_SWITCH ^( I_PACKED_SWITCH_START_KEY fixed_32bit_literal ) packed_switch_elements )
			{
			match(input,I_STATEMENT_PACKED_SWITCH,FOLLOW_I_STATEMENT_PACKED_SWITCH_in_insn_packed_switch_directive3396); 
			match(input, Token.DOWN, null); 
			match(input,I_PACKED_SWITCH_START_KEY,FOLLOW_I_PACKED_SWITCH_START_KEY_in_insn_packed_switch_directive3399); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_packed_switch_directive3401);
			fixed_32bit_literal206=fixed_32bit_literal();
			state._fsp--;

			match(input, Token.UP, null); 

			pushFollow(FOLLOW_packed_switch_elements_in_insn_packed_switch_directive3404);
			packed_switch_elements207=packed_switch_elements();
			state._fsp--;

			match(input, Token.UP, null); 


			        int startKey = fixed_32bit_literal206;
			        method_stack.peek().methodBuilder.addInstruction(new BuilderPackedSwitchPayload(startKey,
			            packed_switch_elements207));
			      
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_packed_switch_directive"



	// $ANTLR start "insn_sparse_switch_directive"
	// smaliTreeWalker.g:1288:1: insn_sparse_switch_directive : ^( I_STATEMENT_SPARSE_SWITCH sparse_switch_elements ) ;
	public final void insn_sparse_switch_directive() throws RecognitionException {
		List<SwitchLabelElement> sparse_switch_elements208 =null;

		try {
			// smaliTreeWalker.g:1289:3: ( ^( I_STATEMENT_SPARSE_SWITCH sparse_switch_elements ) )
			// smaliTreeWalker.g:1290:5: ^( I_STATEMENT_SPARSE_SWITCH sparse_switch_elements )
			{
			match(input,I_STATEMENT_SPARSE_SWITCH,FOLLOW_I_STATEMENT_SPARSE_SWITCH_in_insn_sparse_switch_directive3428); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_sparse_switch_elements_in_insn_sparse_switch_directive3430);
			sparse_switch_elements208=sparse_switch_elements();
			state._fsp--;

			match(input, Token.UP, null); 


			      method_stack.peek().methodBuilder.addInstruction(new BuilderSparseSwitchPayload(sparse_switch_elements208));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "insn_sparse_switch_directive"



	// $ANTLR start "array_descriptor"
	// smaliTreeWalker.g:1295:1: array_descriptor returns [String type] : ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR ) ;
	public final String array_descriptor() throws RecognitionException {
		String type = null;


		CommonTree ARRAY_TYPE_PREFIX209=null;
		CommonTree PRIMITIVE_TYPE210=null;
		CommonTree CLASS_DESCRIPTOR211=null;

		try {
			// smaliTreeWalker.g:1296:3: ( ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR ) )
			// smaliTreeWalker.g:1296:5: ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR )
			{
			ARRAY_TYPE_PREFIX209=(CommonTree)match(input,ARRAY_TYPE_PREFIX,FOLLOW_ARRAY_TYPE_PREFIX_in_array_descriptor3451); 
			// smaliTreeWalker.g:1296:23: ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR )
			int alt39=2;
			int LA39_0 = input.LA(1);
			if ( (LA39_0==PRIMITIVE_TYPE) ) {
				alt39=1;
			}
			else if ( (LA39_0==CLASS_DESCRIPTOR) ) {
				alt39=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 39, 0, input);
				throw nvae;
			}

			switch (alt39) {
				case 1 :
					// smaliTreeWalker.g:1296:25: PRIMITIVE_TYPE
					{
					PRIMITIVE_TYPE210=(CommonTree)match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_array_descriptor3455); 
					 type = (ARRAY_TYPE_PREFIX209!=null?ARRAY_TYPE_PREFIX209.getText():null) + (PRIMITIVE_TYPE210!=null?PRIMITIVE_TYPE210.getText():null); 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1297:25: CLASS_DESCRIPTOR
					{
					CLASS_DESCRIPTOR211=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_array_descriptor3483); 
					 type = (ARRAY_TYPE_PREFIX209!=null?ARRAY_TYPE_PREFIX209.getText():null) + (CLASS_DESCRIPTOR211!=null?CLASS_DESCRIPTOR211.getText():null); 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return type;
	}
	// $ANTLR end "array_descriptor"


	public static class nonvoid_type_descriptor_return extends TreeRuleReturnScope {
		public String type;
	};


	// $ANTLR start "nonvoid_type_descriptor"
	// smaliTreeWalker.g:1299:1: nonvoid_type_descriptor returns [String type] : ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor ) ;
	public final smaliTreeWalker.nonvoid_type_descriptor_return nonvoid_type_descriptor() throws RecognitionException {
		smaliTreeWalker.nonvoid_type_descriptor_return retval = new smaliTreeWalker.nonvoid_type_descriptor_return();
		retval.start = input.LT(1);

		String array_descriptor212 =null;

		try {
			// smaliTreeWalker.g:1300:3: ( ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor ) )
			// smaliTreeWalker.g:1300:5: ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor )
			{
			// smaliTreeWalker.g:1300:5: ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor )
			int alt40=3;
			switch ( input.LA(1) ) {
			case PRIMITIVE_TYPE:
				{
				alt40=1;
				}
				break;
			case CLASS_DESCRIPTOR:
				{
				alt40=2;
				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				alt40=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 40, 0, input);
				throw nvae;
			}
			switch (alt40) {
				case 1 :
					// smaliTreeWalker.g:1300:6: PRIMITIVE_TYPE
					{
					match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_nonvoid_type_descriptor3501); 
					 retval.type = input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(retval.start),input.getTreeAdaptor().getTokenStopIndex(retval.start)); 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1301:5: CLASS_DESCRIPTOR
					{
					match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_nonvoid_type_descriptor3509); 
					 retval.type = input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(retval.start),input.getTreeAdaptor().getTokenStopIndex(retval.start)); 
					}
					break;
				case 3 :
					// smaliTreeWalker.g:1302:5: array_descriptor
					{
					pushFollow(FOLLOW_array_descriptor_in_nonvoid_type_descriptor3517);
					array_descriptor212=array_descriptor();
					state._fsp--;

					 retval.type = array_descriptor212; 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "nonvoid_type_descriptor"


	public static class reference_type_descriptor_return extends TreeRuleReturnScope {
		public String type;
	};


	// $ANTLR start "reference_type_descriptor"
	// smaliTreeWalker.g:1305:1: reference_type_descriptor returns [String type] : ( CLASS_DESCRIPTOR | array_descriptor ) ;
	public final smaliTreeWalker.reference_type_descriptor_return reference_type_descriptor() throws RecognitionException {
		smaliTreeWalker.reference_type_descriptor_return retval = new smaliTreeWalker.reference_type_descriptor_return();
		retval.start = input.LT(1);

		String array_descriptor213 =null;

		try {
			// smaliTreeWalker.g:1306:3: ( ( CLASS_DESCRIPTOR | array_descriptor ) )
			// smaliTreeWalker.g:1306:5: ( CLASS_DESCRIPTOR | array_descriptor )
			{
			// smaliTreeWalker.g:1306:5: ( CLASS_DESCRIPTOR | array_descriptor )
			int alt41=2;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==CLASS_DESCRIPTOR) ) {
				alt41=1;
			}
			else if ( (LA41_0==ARRAY_TYPE_PREFIX) ) {
				alt41=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 41, 0, input);
				throw nvae;
			}

			switch (alt41) {
				case 1 :
					// smaliTreeWalker.g:1306:6: CLASS_DESCRIPTOR
					{
					match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_reference_type_descriptor3538); 
					 retval.type = input.getTokenStream().toString(input.getTreeAdaptor().getTokenStartIndex(retval.start),input.getTreeAdaptor().getTokenStopIndex(retval.start)); 
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1307:5: array_descriptor
					{
					pushFollow(FOLLOW_array_descriptor_in_reference_type_descriptor3546);
					array_descriptor213=array_descriptor();
					state._fsp--;

					 retval.type = array_descriptor213; 
					}
					break;

			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "reference_type_descriptor"



	// $ANTLR start "type_descriptor"
	// smaliTreeWalker.g:1310:1: type_descriptor returns [String type] : ( VOID_TYPE | nonvoid_type_descriptor );
	public final String type_descriptor() throws RecognitionException {
		String type = null;


		TreeRuleReturnScope nonvoid_type_descriptor214 =null;

		try {
			// smaliTreeWalker.g:1311:3: ( VOID_TYPE | nonvoid_type_descriptor )
			int alt42=2;
			int LA42_0 = input.LA(1);
			if ( (LA42_0==VOID_TYPE) ) {
				alt42=1;
			}
			else if ( (LA42_0==ARRAY_TYPE_PREFIX||LA42_0==CLASS_DESCRIPTOR||LA42_0==PRIMITIVE_TYPE) ) {
				alt42=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 42, 0, input);
				throw nvae;
			}

			switch (alt42) {
				case 1 :
					// smaliTreeWalker.g:1311:5: VOID_TYPE
					{
					match(input,VOID_TYPE,FOLLOW_VOID_TYPE_in_type_descriptor3566); 
					type = "V";
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1312:5: nonvoid_type_descriptor
					{
					pushFollow(FOLLOW_nonvoid_type_descriptor_in_type_descriptor3574);
					nonvoid_type_descriptor214=nonvoid_type_descriptor();
					state._fsp--;

					type = (nonvoid_type_descriptor214!=null?((smaliTreeWalker.nonvoid_type_descriptor_return)nonvoid_type_descriptor214).type:null);
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return type;
	}
	// $ANTLR end "type_descriptor"



	// $ANTLR start "short_integral_literal"
	// smaliTreeWalker.g:1315:1: short_integral_literal returns [short value] : ( long_literal | integer_literal | short_literal | char_literal | byte_literal );
	public final short short_integral_literal() throws RecognitionException {
		short value = 0;


		long long_literal215 =0;
		int integer_literal216 =0;
		short short_literal217 =0;
		char char_literal218 =0;
		byte byte_literal219 =0;

		try {
			// smaliTreeWalker.g:1316:3: ( long_literal | integer_literal | short_literal | char_literal | byte_literal )
			int alt43=5;
			switch ( input.LA(1) ) {
			case LONG_LITERAL:
				{
				alt43=1;
				}
				break;
			case INTEGER_LITERAL:
				{
				alt43=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt43=3;
				}
				break;
			case CHAR_LITERAL:
				{
				alt43=4;
				}
				break;
			case BYTE_LITERAL:
				{
				alt43=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 43, 0, input);
				throw nvae;
			}
			switch (alt43) {
				case 1 :
					// smaliTreeWalker.g:1316:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_short_integral_literal3592);
					long_literal215=long_literal();
					state._fsp--;


					      LiteralTools.checkShort(long_literal215);
					      value = (short)long_literal215;
					    
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1321:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_short_integral_literal3604);
					integer_literal216=integer_literal();
					state._fsp--;


					      LiteralTools.checkShort(integer_literal216);
					      value = (short)integer_literal216;
					    
					}
					break;
				case 3 :
					// smaliTreeWalker.g:1326:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_short_integral_literal3616);
					short_literal217=short_literal();
					state._fsp--;

					value = short_literal217;
					}
					break;
				case 4 :
					// smaliTreeWalker.g:1327:5: char_literal
					{
					pushFollow(FOLLOW_char_literal_in_short_integral_literal3624);
					char_literal218=char_literal();
					state._fsp--;

					value = (short)char_literal218;
					}
					break;
				case 5 :
					// smaliTreeWalker.g:1328:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_short_integral_literal3632);
					byte_literal219=byte_literal();
					state._fsp--;

					value = byte_literal219;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "short_integral_literal"



	// $ANTLR start "integral_literal"
	// smaliTreeWalker.g:1330:1: integral_literal returns [int value] : ( long_literal | integer_literal | short_literal | byte_literal );
	public final int integral_literal() throws RecognitionException {
		int value = 0;


		long long_literal220 =0;
		int integer_literal221 =0;
		short short_literal222 =0;
		byte byte_literal223 =0;

		try {
			// smaliTreeWalker.g:1331:3: ( long_literal | integer_literal | short_literal | byte_literal )
			int alt44=4;
			switch ( input.LA(1) ) {
			case LONG_LITERAL:
				{
				alt44=1;
				}
				break;
			case INTEGER_LITERAL:
				{
				alt44=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt44=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt44=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}
			switch (alt44) {
				case 1 :
					// smaliTreeWalker.g:1331:5: long_literal
					{
					pushFollow(FOLLOW_long_literal_in_integral_literal3647);
					long_literal220=long_literal();
					state._fsp--;


					      LiteralTools.checkInt(long_literal220);
					      value = (int)long_literal220;
					    
					}
					break;
				case 2 :
					// smaliTreeWalker.g:1336:5: integer_literal
					{
					pushFollow(FOLLOW_integer_literal_in_integral_literal3659);
					integer_literal221=integer_literal();
					state._fsp--;

					value = integer_literal221;
					}
					break;
				case 3 :
					// smaliTreeWalker.g:1337:5: short_literal
					{
					pushFollow(FOLLOW_short_literal_in_integral_literal3667);
					short_literal222=short_literal();
					state._fsp--;

					value = short_literal222;
					}
					break;
				case 4 :
					// smaliTreeWalker.g:1338:5: byte_literal
					{
					pushFollow(FOLLOW_byte_literal_in_integral_literal3675);
					byte_literal223=byte_literal();
					state._fsp--;

					value = byte_literal223;
					}
					break;

			}
		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "integral_literal"



	// $ANTLR start "integer_literal"
	// smaliTreeWalker.g:1341:1: integer_literal returns [int value] : INTEGER_LITERAL ;
	public final int integer_literal() throws RecognitionException {
		int value = 0;


		CommonTree INTEGER_LITERAL224=null;

		try {
			// smaliTreeWalker.g:1342:3: ( INTEGER_LITERAL )
			// smaliTreeWalker.g:1342:5: INTEGER_LITERAL
			{
			INTEGER_LITERAL224=(CommonTree)match(input,INTEGER_LITERAL,FOLLOW_INTEGER_LITERAL_in_integer_literal3691); 
			 value = LiteralTools.parseInt((INTEGER_LITERAL224!=null?INTEGER_LITERAL224.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "integer_literal"



	// $ANTLR start "long_literal"
	// smaliTreeWalker.g:1344:1: long_literal returns [long value] : LONG_LITERAL ;
	public final long long_literal() throws RecognitionException {
		long value = 0;


		CommonTree LONG_LITERAL225=null;

		try {
			// smaliTreeWalker.g:1345:3: ( LONG_LITERAL )
			// smaliTreeWalker.g:1345:5: LONG_LITERAL
			{
			LONG_LITERAL225=(CommonTree)match(input,LONG_LITERAL,FOLLOW_LONG_LITERAL_in_long_literal3706); 
			 value = LiteralTools.parseLong((LONG_LITERAL225!=null?LONG_LITERAL225.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "long_literal"



	// $ANTLR start "short_literal"
	// smaliTreeWalker.g:1347:1: short_literal returns [short value] : SHORT_LITERAL ;
	public final short short_literal() throws RecognitionException {
		short value = 0;


		CommonTree SHORT_LITERAL226=null;

		try {
			// smaliTreeWalker.g:1348:3: ( SHORT_LITERAL )
			// smaliTreeWalker.g:1348:5: SHORT_LITERAL
			{
			SHORT_LITERAL226=(CommonTree)match(input,SHORT_LITERAL,FOLLOW_SHORT_LITERAL_in_short_literal3721); 
			 value = LiteralTools.parseShort((SHORT_LITERAL226!=null?SHORT_LITERAL226.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "short_literal"



	// $ANTLR start "byte_literal"
	// smaliTreeWalker.g:1350:1: byte_literal returns [byte value] : BYTE_LITERAL ;
	public final byte byte_literal() throws RecognitionException {
		byte value = 0;


		CommonTree BYTE_LITERAL227=null;

		try {
			// smaliTreeWalker.g:1351:3: ( BYTE_LITERAL )
			// smaliTreeWalker.g:1351:5: BYTE_LITERAL
			{
			BYTE_LITERAL227=(CommonTree)match(input,BYTE_LITERAL,FOLLOW_BYTE_LITERAL_in_byte_literal3736); 
			 value = LiteralTools.parseByte((BYTE_LITERAL227!=null?BYTE_LITERAL227.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "byte_literal"



	// $ANTLR start "float_literal"
	// smaliTreeWalker.g:1353:1: float_literal returns [float value] : FLOAT_LITERAL ;
	public final float float_literal() throws RecognitionException {
		float value = 0.0f;


		CommonTree FLOAT_LITERAL228=null;

		try {
			// smaliTreeWalker.g:1354:3: ( FLOAT_LITERAL )
			// smaliTreeWalker.g:1354:5: FLOAT_LITERAL
			{
			FLOAT_LITERAL228=(CommonTree)match(input,FLOAT_LITERAL,FOLLOW_FLOAT_LITERAL_in_float_literal3751); 
			 value = LiteralTools.parseFloat((FLOAT_LITERAL228!=null?FLOAT_LITERAL228.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "float_literal"



	// $ANTLR start "double_literal"
	// smaliTreeWalker.g:1356:1: double_literal returns [double value] : DOUBLE_LITERAL ;
	public final double double_literal() throws RecognitionException {
		double value = 0.0;


		CommonTree DOUBLE_LITERAL229=null;

		try {
			// smaliTreeWalker.g:1357:3: ( DOUBLE_LITERAL )
			// smaliTreeWalker.g:1357:5: DOUBLE_LITERAL
			{
			DOUBLE_LITERAL229=(CommonTree)match(input,DOUBLE_LITERAL,FOLLOW_DOUBLE_LITERAL_in_double_literal3766); 
			 value = LiteralTools.parseDouble((DOUBLE_LITERAL229!=null?DOUBLE_LITERAL229.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "double_literal"



	// $ANTLR start "char_literal"
	// smaliTreeWalker.g:1359:1: char_literal returns [char value] : CHAR_LITERAL ;
	public final char char_literal() throws RecognitionException {
		char value = 0;


		CommonTree CHAR_LITERAL230=null;

		try {
			// smaliTreeWalker.g:1360:3: ( CHAR_LITERAL )
			// smaliTreeWalker.g:1360:5: CHAR_LITERAL
			{
			CHAR_LITERAL230=(CommonTree)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_char_literal3781); 
			 value = (CHAR_LITERAL230!=null?CHAR_LITERAL230.getText():null).charAt(1); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "char_literal"



	// $ANTLR start "string_literal"
	// smaliTreeWalker.g:1362:1: string_literal returns [String value] : STRING_LITERAL ;
	public final String string_literal() throws RecognitionException {
		String value = null;


		CommonTree STRING_LITERAL231=null;

		try {
			// smaliTreeWalker.g:1363:3: ( STRING_LITERAL )
			// smaliTreeWalker.g:1363:5: STRING_LITERAL
			{
			STRING_LITERAL231=(CommonTree)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_string_literal3796); 

			      value = (STRING_LITERAL231!=null?STRING_LITERAL231.getText():null);
			      value = value.substring(1,value.length()-1);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "string_literal"



	// $ANTLR start "bool_literal"
	// smaliTreeWalker.g:1369:1: bool_literal returns [boolean value] : BOOL_LITERAL ;
	public final boolean bool_literal() throws RecognitionException {
		boolean value = false;


		CommonTree BOOL_LITERAL232=null;

		try {
			// smaliTreeWalker.g:1370:3: ( BOOL_LITERAL )
			// smaliTreeWalker.g:1370:5: BOOL_LITERAL
			{
			BOOL_LITERAL232=(CommonTree)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_bool_literal3815); 
			 value = Boolean.parseBoolean((BOOL_LITERAL232!=null?BOOL_LITERAL232.getText():null)); 
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "bool_literal"



	// $ANTLR start "array_literal"
	// smaliTreeWalker.g:1372:1: array_literal returns [List<EncodedValue> elements] : ^( I_ENCODED_ARRAY ( literal )* ) ;
	public final List<EncodedValue> array_literal() throws RecognitionException {
		List<EncodedValue> elements = null;


		ImmutableEncodedValue literal233 =null;

		try {
			// smaliTreeWalker.g:1373:3: ( ^( I_ENCODED_ARRAY ( literal )* ) )
			// smaliTreeWalker.g:1373:5: ^( I_ENCODED_ARRAY ( literal )* )
			{
			elements = Lists.newArrayList();
			match(input,I_ENCODED_ARRAY,FOLLOW_I_ENCODED_ARRAY_in_array_literal3837); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:1374:23: ( literal )*
				loop45:
				while (true) {
					int alt45=2;
					int LA45_0 = input.LA(1);
					if ( (LA45_0==ARRAY_TYPE_PREFIX||(LA45_0 >= BOOL_LITERAL && LA45_0 <= BYTE_LITERAL)||(LA45_0 >= CHAR_LITERAL && LA45_0 <= CLASS_DESCRIPTOR)||LA45_0==DOUBLE_LITERAL||LA45_0==FLOAT_LITERAL||LA45_0==INTEGER_LITERAL||(LA45_0 >= I_ENCODED_ARRAY && LA45_0 <= I_ENCODED_METHOD_HANDLE)||LA45_0==I_METHOD_PROTOTYPE||LA45_0==I_SUBANNOTATION||LA45_0==LONG_LITERAL||LA45_0==NULL_LITERAL||LA45_0==PRIMITIVE_TYPE||LA45_0==SHORT_LITERAL||LA45_0==STRING_LITERAL||LA45_0==VOID_TYPE) ) {
						alt45=1;
					}

					switch (alt45) {
					case 1 :
						// smaliTreeWalker.g:1374:24: literal
						{
						pushFollow(FOLLOW_literal_in_array_literal3840);
						literal233=literal();
						state._fsp--;

						elements.add(literal233);
						}
						break;

					default :
						break loop45;
					}
				}

				match(input, Token.UP, null); 
			}

			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return elements;
	}
	// $ANTLR end "array_literal"



	// $ANTLR start "annotations"
	// smaliTreeWalker.g:1376:1: annotations returns [Set<Annotation> annotations] : ^( I_ANNOTATIONS ( annotation )* ) ;
	public final Set<Annotation> annotations() throws RecognitionException {
		Set<Annotation> annotations = null;


		Annotation annotation234 =null;

		try {
			// smaliTreeWalker.g:1377:3: ( ^( I_ANNOTATIONS ( annotation )* ) )
			// smaliTreeWalker.g:1377:5: ^( I_ANNOTATIONS ( annotation )* )
			{
			HashMap<String, Annotation> annotationMap = Maps.newHashMap();
			match(input,I_ANNOTATIONS,FOLLOW_I_ANNOTATIONS_in_annotations3865); 
			if ( input.LA(1)==Token.DOWN ) {
				match(input, Token.DOWN, null); 
				// smaliTreeWalker.g:1378:21: ( annotation )*
				loop46:
				while (true) {
					int alt46=2;
					int LA46_0 = input.LA(1);
					if ( (LA46_0==I_ANNOTATION) ) {
						alt46=1;
					}

					switch (alt46) {
					case 1 :
						// smaliTreeWalker.g:1378:22: annotation
						{
						pushFollow(FOLLOW_annotation_in_annotations3868);
						annotation234=annotation();
						state._fsp--;


						        Annotation anno = annotation234;
						        Annotation old = annotationMap.put(anno.getType(), anno);
						        if (old != null) {
						            throw new SemanticException(input, "Multiple annotations of type %s", anno.getType());
						        }
						    
						}
						break;

					default :
						break loop46;
					}
				}

				match(input, Token.UP, null); 
			}


			        annotations = ImmutableSet.copyOf(annotationMap.values());
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return annotations;
	}
	// $ANTLR end "annotations"



	// $ANTLR start "annotation"
	// smaliTreeWalker.g:1390:1: annotation returns [Annotation annotation] : ^( I_ANNOTATION ANNOTATION_VISIBILITY subannotation ) ;
	public final Annotation annotation() throws RecognitionException {
		Annotation annotation = null;


		CommonTree ANNOTATION_VISIBILITY235=null;
		TreeRuleReturnScope subannotation236 =null;

		try {
			// smaliTreeWalker.g:1391:3: ( ^( I_ANNOTATION ANNOTATION_VISIBILITY subannotation ) )
			// smaliTreeWalker.g:1391:5: ^( I_ANNOTATION ANNOTATION_VISIBILITY subannotation )
			{
			match(input,I_ANNOTATION,FOLLOW_I_ANNOTATION_in_annotation3897); 
			match(input, Token.DOWN, null); 
			ANNOTATION_VISIBILITY235=(CommonTree)match(input,ANNOTATION_VISIBILITY,FOLLOW_ANNOTATION_VISIBILITY_in_annotation3899); 
			pushFollow(FOLLOW_subannotation_in_annotation3901);
			subannotation236=subannotation();
			state._fsp--;

			match(input, Token.UP, null); 


			      int visibility = AnnotationVisibility.getVisibility((ANNOTATION_VISIBILITY235!=null?ANNOTATION_VISIBILITY235.getText():null));
			      annotation = new ImmutableAnnotation(visibility, (subannotation236!=null?((smaliTreeWalker.subannotation_return)subannotation236).annotationType:null), (subannotation236!=null?((smaliTreeWalker.subannotation_return)subannotation236).elements:null));
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return annotation;
	}
	// $ANTLR end "annotation"



	// $ANTLR start "annotation_element"
	// smaliTreeWalker.g:1397:1: annotation_element returns [AnnotationElement element] : ^( I_ANNOTATION_ELEMENT SIMPLE_NAME literal ) ;
	public final AnnotationElement annotation_element() throws RecognitionException {
		AnnotationElement element = null;


		CommonTree SIMPLE_NAME237=null;
		ImmutableEncodedValue literal238 =null;

		try {
			// smaliTreeWalker.g:1398:3: ( ^( I_ANNOTATION_ELEMENT SIMPLE_NAME literal ) )
			// smaliTreeWalker.g:1398:5: ^( I_ANNOTATION_ELEMENT SIMPLE_NAME literal )
			{
			match(input,I_ANNOTATION_ELEMENT,FOLLOW_I_ANNOTATION_ELEMENT_in_annotation_element3922); 
			match(input, Token.DOWN, null); 
			SIMPLE_NAME237=(CommonTree)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_annotation_element3924); 
			pushFollow(FOLLOW_literal_in_annotation_element3926);
			literal238=literal();
			state._fsp--;

			match(input, Token.UP, null); 


			      element = new ImmutableAnnotationElement((SIMPLE_NAME237!=null?SIMPLE_NAME237.getText():null), literal238);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return element;
	}
	// $ANTLR end "annotation_element"


	public static class subannotation_return extends TreeRuleReturnScope {
		public String annotationType;
		public List<AnnotationElement> elements;
	};


	// $ANTLR start "subannotation"
	// smaliTreeWalker.g:1403:1: subannotation returns [String annotationType, List<AnnotationElement> elements] : ^( I_SUBANNOTATION CLASS_DESCRIPTOR ( annotation_element )* ) ;
	public final smaliTreeWalker.subannotation_return subannotation() throws RecognitionException {
		smaliTreeWalker.subannotation_return retval = new smaliTreeWalker.subannotation_return();
		retval.start = input.LT(1);

		CommonTree CLASS_DESCRIPTOR240=null;
		AnnotationElement annotation_element239 =null;

		try {
			// smaliTreeWalker.g:1404:3: ( ^( I_SUBANNOTATION CLASS_DESCRIPTOR ( annotation_element )* ) )
			// smaliTreeWalker.g:1404:5: ^( I_SUBANNOTATION CLASS_DESCRIPTOR ( annotation_element )* )
			{
			ArrayList<AnnotationElement> elements = Lists.newArrayList();
			match(input,I_SUBANNOTATION,FOLLOW_I_SUBANNOTATION_in_subannotation3953); 
			match(input, Token.DOWN, null); 
			CLASS_DESCRIPTOR240=(CommonTree)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_subannotation3963); 
			// smaliTreeWalker.g:1407:9: ( annotation_element )*
			loop47:
			while (true) {
				int alt47=2;
				int LA47_0 = input.LA(1);
				if ( (LA47_0==I_ANNOTATION_ELEMENT) ) {
					alt47=1;
				}

				switch (alt47) {
				case 1 :
					// smaliTreeWalker.g:1407:10: annotation_element
					{
					pushFollow(FOLLOW_annotation_element_in_subannotation3974);
					annotation_element239=annotation_element();
					state._fsp--;


					           elements.add(annotation_element239);
					        
					}
					break;

				default :
					break loop47;
				}
			}

			match(input, Token.UP, null); 


			      retval.annotationType = (CLASS_DESCRIPTOR240!=null?CLASS_DESCRIPTOR240.getText():null);
			      retval.elements = elements;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subannotation"



	// $ANTLR start "field_literal"
	// smaliTreeWalker.g:1417:1: field_literal returns [ImmutableFieldReference value] : ^( I_ENCODED_FIELD field_reference ) ;
	public final ImmutableFieldReference field_literal() throws RecognitionException {
		ImmutableFieldReference value = null;


		TreeRuleReturnScope field_reference241 =null;

		try {
			// smaliTreeWalker.g:1418:3: ( ^( I_ENCODED_FIELD field_reference ) )
			// smaliTreeWalker.g:1418:5: ^( I_ENCODED_FIELD field_reference )
			{
			match(input,I_ENCODED_FIELD,FOLLOW_I_ENCODED_FIELD_in_field_literal4013); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_field_reference_in_field_literal4015);
			field_reference241=field_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      value = (field_reference241!=null?((smaliTreeWalker.field_reference_return)field_reference241).fieldReference:null);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "field_literal"



	// $ANTLR start "method_literal"
	// smaliTreeWalker.g:1423:1: method_literal returns [ImmutableMethodReference value] : ^( I_ENCODED_METHOD method_reference ) ;
	public final ImmutableMethodReference method_literal() throws RecognitionException {
		ImmutableMethodReference value = null;


		ImmutableMethodReference method_reference242 =null;

		try {
			// smaliTreeWalker.g:1424:3: ( ^( I_ENCODED_METHOD method_reference ) )
			// smaliTreeWalker.g:1424:5: ^( I_ENCODED_METHOD method_reference )
			{
			match(input,I_ENCODED_METHOD,FOLLOW_I_ENCODED_METHOD_in_method_literal4036); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_method_reference_in_method_literal4038);
			method_reference242=method_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      value = method_reference242;
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "method_literal"



	// $ANTLR start "enum_literal"
	// smaliTreeWalker.g:1429:1: enum_literal returns [ImmutableFieldReference value] : ^( I_ENCODED_ENUM field_reference ) ;
	public final ImmutableFieldReference enum_literal() throws RecognitionException {
		ImmutableFieldReference value = null;


		TreeRuleReturnScope field_reference243 =null;

		try {
			// smaliTreeWalker.g:1430:3: ( ^( I_ENCODED_ENUM field_reference ) )
			// smaliTreeWalker.g:1430:5: ^( I_ENCODED_ENUM field_reference )
			{
			match(input,I_ENCODED_ENUM,FOLLOW_I_ENCODED_ENUM_in_enum_literal4059); 
			match(input, Token.DOWN, null); 
			pushFollow(FOLLOW_field_reference_in_enum_literal4061);
			field_reference243=field_reference();
			state._fsp--;

			match(input, Token.UP, null); 


			      value = (field_reference243!=null?((smaliTreeWalker.field_reference_return)field_reference243).fieldReference:null);
			    
			}

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
		}
		finally {
			// do for sure before leaving
		}
		return value;
	}
	// $ANTLR end "enum_literal"

	// Delegated rules



	public static final BitSet FOLLOW_I_CLASS_DEF_in_smali_file52 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_header_in_smali_file54 = new BitSet(new long[]{0x0000000000000000L,0x2000000000000000L});
	public static final BitSet FOLLOW_methods_in_smali_file56 = new BitSet(new long[]{0x0000000000000000L,0x0010000000000000L});
	public static final BitSet FOLLOW_fields_in_smali_file58 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_annotations_in_smali_file60 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_class_spec_in_header85 = new BitSet(new long[]{0x0000000000000000L,0x0080000000000000L,0x0040000000000800L});
	public static final BitSet FOLLOW_super_spec_in_header87 = new BitSet(new long[]{0x0000000000000000L,0x0080000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_implements_list_in_header90 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000800L});
	public static final BitSet FOLLOW_source_spec_in_header92 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_class_spec110 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_access_list_in_class_spec112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_SUPER_in_super_spec130 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_super_spec132 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_IMPLEMENTS_in_implements_spec152 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_implements_spec154 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_implements_spec_in_implements_list184 = new BitSet(new long[]{0x0000000000000002L,0x0080000000000000L});
	public static final BitSet FOLLOW_I_SOURCE_in_source_spec213 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_string_literal_in_source_spec215 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_ACCESS_LIST_in_access_list248 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ACCESS_SPEC_in_access_list266 = new BitSet(new long[]{0x0000000000000018L});
	public static final BitSet FOLLOW_I_FIELDS_in_fields308 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_field_in_fields317 = new BitSet(new long[]{0x0000000000000008L,0x0008000000000000L});
	public static final BitSet FOLLOW_I_METHODS_in_methods349 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_method_in_methods358 = new BitSet(new long[]{0x0000000000000008L,0x1000000000000000L});
	public static final BitSet FOLLOW_I_FIELD_in_field383 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_field385 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_access_list_in_field387 = new BitSet(new long[]{0x0000000000000000L,0x0040000000000000L});
	public static final BitSet FOLLOW_I_FIELD_TYPE_in_field390 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_field392 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_field_initial_value_in_field395 = new BitSet(new long[]{0x0000000000000008L,0x0000000400000000L});
	public static final BitSet FOLLOW_annotations_in_field397 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_FIELD_INITIAL_VALUE_in_field_initial_value418 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_literal_in_field_initial_value420 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_integer_literal_in_literal442 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_literal450 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_literal458 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_literal466 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_literal474 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_double_literal_in_literal482 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_char_literal_in_literal490 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_string_literal_in_literal498 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_bool_literal_in_literal506 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NULL_LITERAL_in_literal514 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_descriptor_in_literal522 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_literal_in_literal530 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subannotation_in_literal538 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_field_literal_in_literal546 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_literal_in_literal554 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_literal_in_literal562 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_handle_literal_in_literal570 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_prototype_in_literal578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_fixed_64bit_literal_number594 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_fixed_64bit_literal_number602 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_fixed_64bit_literal_number610 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_fixed_64bit_literal_number618 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_fixed_64bit_literal_number626 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_double_literal_in_fixed_64bit_literal_number634 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_char_literal_in_fixed_64bit_literal_number642 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_bool_literal_in_fixed_64bit_literal_number650 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_fixed_64bit_literal665 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_fixed_64bit_literal673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_fixed_64bit_literal681 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_fixed_64bit_literal689 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_fixed_64bit_literal697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_double_literal_in_fixed_64bit_literal705 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_char_literal_in_fixed_64bit_literal713 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_bool_literal_in_fixed_64bit_literal721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_fixed_32bit_literal738 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_fixed_32bit_literal746 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_fixed_32bit_literal754 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_fixed_32bit_literal762 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_fixed_32bit_literal770 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_char_literal_in_fixed_32bit_literal778 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_bool_literal_in_fixed_32bit_literal786 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_ARRAY_ELEMENTS_in_array_elements808 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_fixed_64bit_literal_number_in_array_elements817 = new BitSet(new long[]{0x0000008000809808L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_I_PACKED_SWITCH_ELEMENTS_in_packed_switch_elements853 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_label_ref_in_packed_switch_elements862 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_I_SPARSE_SWITCH_ELEMENTS_in_sparse_switch_elements897 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_sparse_switch_elements907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_sparse_switch_elements909 = new BitSet(new long[]{0x0000008000009808L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_I_METHOD_in_method961 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_method_name_and_prototype_in_method969 = new BitSet(new long[]{0x0000000000000000L,0x0000000100000000L});
	public static final BitSet FOLLOW_access_list_in_method977 = new BitSet(new long[]{0x0000000000000000L,0x0800000000000000L,0x0000000000000081L});
	public static final BitSet FOLLOW_registers_directive_in_method1004 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000001L});
	public static final BitSet FOLLOW_ordered_method_items_in_method1061 = new BitSet(new long[]{0x0000000000000000L,0x0000040000000000L});
	public static final BitSet FOLLOW_catches_in_method1069 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000010L});
	public static final BitSet FOLLOW_parameters_in_method1077 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_annotations_in_method1086 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_METHOD_PROTOTYPE_in_method_prototype1110 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_I_METHOD_RETURN_TYPE_in_method_prototype1113 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_type_descriptor_in_method_prototype1115 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_method_type_list_in_method_prototype1118 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_method_name_and_prototype1136 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_method_name_and_prototype1138 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_method_type_list1172 = new BitSet(new long[]{0x0000000000010102L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_I_CALL_SITE_REFERENCE_in_call_site_reference1203 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_call_site_reference1207 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_string_literal_in_call_site_reference1211 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_call_site_reference1213 = new BitSet(new long[]{0x0000000000000000L,0x0000004000000000L});
	public static final BitSet FOLLOW_call_site_extra_arguments_in_call_site_reference1223 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_method_reference_in_call_site_reference1225 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_set_in_method_handle_type1245 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_handle_type_in_method_handle_reference1270 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_field_reference_in_method_handle_reference1273 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_reference_in_method_handle_reference1277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_ENCODED_METHOD_HANDLE_in_method_handle_literal1294 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L,0xC000000000000000L});
	public static final BitSet FOLLOW_method_handle_reference_in_method_handle_literal1296 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_method_reference1312 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_method_reference1315 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_method_reference1317 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_field_reference1334 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_field_reference1337 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_field_reference1339 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_REGISTERS_in_registers_directive1365 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_I_LOCALS_in_registers_directive1377 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_short_integral_literal_in_registers_directive1395 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_LABEL_in_label_def1415 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_label_def1417 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_CATCHES_in_catches1443 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_catch_directive_in_catches1445 = new BitSet(new long[]{0x0000000000000008L,0x0000030000000000L});
	public static final BitSet FOLLOW_catchall_directive_in_catches1448 = new BitSet(new long[]{0x0000000000000008L,0x0000020000000000L});
	public static final BitSet FOLLOW_I_CATCH_in_catch_directive1461 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_catch_directive1463 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive1467 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive1471 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive1475 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_CATCHALL_in_catchall_directive1491 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive1495 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive1499 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive1503 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_PARAMETERS_in_parameters1520 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_parameter_in_parameters1523 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_I_PARAMETER_in_parameter1539 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_parameter1541 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_string_literal_in_parameter1543 = new BitSet(new long[]{0x0000000000000000L,0x0000000400000000L});
	public static final BitSet FOLLOW_annotations_in_parameter1546 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_line_in_debug_directive1563 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_local_in_debug_directive1569 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_end_local_in_debug_directive1575 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_restart_local_in_debug_directive1581 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prologue_in_debug_directive1587 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_epilogue_in_debug_directive1593 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_source_in_debug_directive1599 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_LINE_in_line1610 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_integral_literal_in_line1612 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_LOCAL_in_local1630 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_local1632 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000000L,0x0000000000020002L});
	public static final BitSet FOLLOW_NULL_LITERAL_in_local1636 = new BitSet(new long[]{0x0000000000010108L,0x0000000000000000L,0x0000000000000000L,0x0000000000020100L});
	public static final BitSet FOLLOW_string_literal_in_local1642 = new BitSet(new long[]{0x0000000000010108L,0x0000000000000000L,0x0000000000000000L,0x0000000000020100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_local1645 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_string_literal_in_local1650 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_END_LOCAL_in_end_local1671 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_end_local1673 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_RESTART_LOCAL_in_restart_local1691 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_restart_local1693 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_PROLOGUE_in_prologue1710 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_EPILOGUE_in_epilogue1726 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_SOURCE_in_source1743 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_string_literal_in_source1745 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_CALL_SITE_EXTRA_ARGUMENTS_in_call_site_extra_arguments1771 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_literal_in_call_site_extra_arguments1774 = new BitSet(new long[]{0x0000008000819908L,0x4001F00040000000L,0x0820000000000000L,0x0000000000222102L});
	public static final BitSet FOLLOW_I_ORDERED_METHOD_ITEMS_in_ordered_method_items1790 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_label_def_in_ordered_method_items1793 = new BitSet(new long[]{0x0000000000000008L,0x0706000000000000L,0x001FFFFFFFFFEC40L});
	public static final BitSet FOLLOW_instruction_in_ordered_method_items1797 = new BitSet(new long[]{0x0000000000000008L,0x0706000000000000L,0x001FFFFFFFFFEC40L});
	public static final BitSet FOLLOW_debug_directive_in_ordered_method_items1801 = new BitSet(new long[]{0x0000000000000008L,0x0706000000000000L,0x001FFFFFFFFFEC40L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_label_ref1817 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_REGISTER_LIST_in_register_list1842 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_register_list1851 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_I_REGISTER_RANGE_in_register_range1876 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_REGISTER_in_register_range1881 = new BitSet(new long[]{0x0000000000000008L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_register_range1885 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_verification_error_reference1908 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_field_reference_in_verification_error_reference1918 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_reference_in_verification_error_reference1928 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VERIFICATION_ERROR_TYPE_in_verification_error_type1945 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format10t_in_instruction1959 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format10x_in_instruction1965 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format11n_in_instruction1971 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format11x_in_instruction1977 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format12x_in_instruction1983 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format20bc_in_instruction1989 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format20t_in_instruction1995 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_field_in_instruction2001 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_method_handle_in_instruction2007 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_method_type_in_instruction2013 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_string_in_instruction2019 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_type_in_instruction2025 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21ih_in_instruction2031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21lh_in_instruction2037 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21s_in_instruction2043 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21t_in_instruction2049 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22b_in_instruction2055 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22c_field_in_instruction2061 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22c_type_in_instruction2067 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22s_in_instruction2073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22t_in_instruction2079 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22x_in_instruction2085 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format23x_in_instruction2091 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format30t_in_instruction2097 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31c_in_instruction2103 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31i_in_instruction2109 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31t_in_instruction2115 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format32x_in_instruction2121 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_call_site_in_instruction2127 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_method_in_instruction2133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_type_in_instruction2139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_call_site_in_instruction2145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_method_in_instruction2151 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_type_in_instruction2157 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format45cc_method_in_instruction2163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format4rcc_method_in_instruction2169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format51l_type_in_instruction2175 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_array_data_directive_in_instruction2181 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_packed_switch_directive_in_instruction2187 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_sparse_switch_directive_in_instruction2193 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT10t_in_insn_format10t2217 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10t_in_insn_format10t2219 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format10t2221 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT10x_in_insn_format10x2244 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10x_in_insn_format10x2246 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT11n_in_insn_format11n2269 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT11n_in_insn_format11n2271 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format11n2273 = new BitSet(new long[]{0x0000000000009000L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_short_integral_literal_in_insn_format11n2275 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT11x_in_insn_format11x2298 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT11x_in_insn_format11x2300 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format11x2302 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT12x_in_insn_format12x2325 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT12x_in_insn_format12x2327 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format12x2331 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format12x2335 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT20bc_in_insn_format20bc2358 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT20bc_in_insn_format20bc2360 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_verification_error_type_in_insn_format20bc2362 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_verification_error_reference_in_insn_format20bc2364 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT20t_in_insn_format20t2387 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT20t_in_insn_format20t2389 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format20t2391 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21c_FIELD_in_insn_format21c_field2414 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_set_in_insn_format21c_field2418 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_field2426 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_field_reference_in_insn_format21c_field2428 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle2451 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle2456 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_method_handle2459 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L,0xC000000000000000L});
	public static final BitSet FOLLOW_method_handle_reference_in_insn_format21c_method_handle2461 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type2484 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type2489 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_method_type2492 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format21c_method_type2494 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21c_STRING_in_insn_format21c_string2517 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_insn_format21c_string2519 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_string2521 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_string_literal_in_insn_format21c_string2523 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21c_TYPE_in_insn_format21c_type2546 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_insn_format21c_type2548 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_type2550 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format21c_type2552 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21ih_in_insn_format21ih2575 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21ih_in_insn_format21ih2577 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21ih2579 = new BitSet(new long[]{0x0000008000009800L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_format21ih2581 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21lh_in_insn_format21lh2604 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21lh_in_insn_format21lh2606 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21lh2608 = new BitSet(new long[]{0x0000008000809800L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_fixed_64bit_literal_in_insn_format21lh2610 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21s_in_insn_format21s2633 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21s_in_insn_format21s2635 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21s2637 = new BitSet(new long[]{0x0000000000009000L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_short_integral_literal_in_insn_format21s2639 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT21t_in_insn_format21t2662 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21t_in_insn_format21t2664 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21t2666 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format21t2668 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22b_in_insn_format22b2691 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22b_in_insn_format22b2693 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22b2697 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22b2701 = new BitSet(new long[]{0x0000000000009000L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_short_integral_literal_in_insn_format22b2703 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22c_FIELD_in_insn_format22c_field2726 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_set_in_insn_format22c_field2730 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field2740 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field2744 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_field_reference_in_insn_format22c_field2746 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22c_TYPE_in_insn_format22c_type2769 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_insn_format22c_type2771 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_type2775 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_type2779 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format22c_type2781 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22s_in_insn_format22s2804 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22s_in_insn_format22s2806 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22s2810 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22s2814 = new BitSet(new long[]{0x0000000000009000L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_short_integral_literal_in_insn_format22s2816 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22t_in_insn_format22t2839 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22t_in_insn_format22t2841 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22t2845 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22t2849 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format22t2851 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT22x_in_insn_format22x2874 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22x_in_insn_format22x2876 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22x2880 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22x2884 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT23x_in_insn_format23x2907 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT23x_in_insn_format23x2909 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x2913 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x2917 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x2921 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT30t_in_insn_format30t2944 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT30t_in_insn_format30t2946 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format30t2948 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT31c_in_insn_format31c2971 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31c_in_insn_format31c2973 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31c2975 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_string_literal_in_insn_format31c2977 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT31i_in_insn_format31i3000 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31i_in_insn_format31i3002 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31i3004 = new BitSet(new long[]{0x0000008000009800L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_format31i3006 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT31t_in_insn_format31t3029 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31t_in_insn_format31t3031 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31t3033 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format31t3035 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT32x_in_insn_format32x3058 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT32x_in_insn_format32x3060 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format32x3064 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format32x3068 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT35c_CALL_SITE_in_insn_format35c_call_site3096 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_insn_format35c_call_site3098 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_call_site3100 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
	public static final BitSet FOLLOW_call_site_reference_in_insn_format35c_call_site3102 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT35c_METHOD_in_insn_format35c_method3125 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_insn_format35c_method3127 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_method3129 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_method_reference_in_insn_format35c_method3131 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT35c_TYPE_in_insn_format35c_type3154 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_insn_format35c_type3156 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_type3158 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format35c_type3160 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site3188 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site3190 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_call_site3192 = new BitSet(new long[]{0x0000000000000000L,0x0000008000000000L});
	public static final BitSet FOLLOW_call_site_reference_in_insn_format3rc_call_site3194 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT3rc_METHOD_in_insn_format3rc_method3217 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_in_insn_format3rc_method3219 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_method3221 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_method_reference_in_insn_format3rc_method3223 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT3rc_TYPE_in_insn_format3rc_type3246 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_TYPE_in_insn_format3rc_type3248 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_type3250 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format3rc_type3252 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT45cc_METHOD_in_insn_format45cc_method3275 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_insn_format45cc_method3277 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_register_list_in_insn_format45cc_method3279 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_method_reference_in_insn_format45cc_method3281 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format45cc_method3283 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT4rcc_METHOD_in_insn_format4rcc_method3306 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_insn_format4rcc_method3308 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000200L});
	public static final BitSet FOLLOW_register_range_in_insn_format4rcc_method3310 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000004000L});
	public static final BitSet FOLLOW_method_reference_in_insn_format4rcc_method3312 = new BitSet(new long[]{0x0000000000000000L,0x4000000000000000L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format4rcc_method3314 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_FORMAT51l_in_insn_format51l_type3337 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT51l_in_insn_format51l_type3339 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format51l_type3341 = new BitSet(new long[]{0x0000008000809800L,0x0000000040000000L,0x0800000000000000L,0x0000000000002000L});
	public static final BitSet FOLLOW_fixed_64bit_literal_in_insn_format51l_type3343 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_ARRAY_DATA_in_insn_array_data_directive3366 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_I_ARRAY_ELEMENT_SIZE_in_insn_array_data_directive3369 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_short_integral_literal_in_insn_array_data_directive3371 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_array_elements_in_insn_array_data_directive3374 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_PACKED_SWITCH_in_insn_packed_switch_directive3396 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_I_PACKED_SWITCH_START_KEY_in_insn_packed_switch_directive3399 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_packed_switch_directive3401 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_packed_switch_elements_in_insn_packed_switch_directive3404 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_STATEMENT_SPARSE_SWITCH_in_insn_sparse_switch_directive3428 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_sparse_switch_elements_in_insn_sparse_switch_directive3430 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_ARRAY_TYPE_PREFIX_in_array_descriptor3451 = new BitSet(new long[]{0x0000000000010000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_array_descriptor3455 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_array_descriptor3483 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_nonvoid_type_descriptor3501 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_nonvoid_type_descriptor3509 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_descriptor_in_nonvoid_type_descriptor3517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_reference_type_descriptor3538 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_descriptor_in_reference_type_descriptor3546 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_TYPE_in_type_descriptor3566 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_type_descriptor3574 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_short_integral_literal3592 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_short_integral_literal3604 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_short_integral_literal3616 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_char_literal_in_short_integral_literal3624 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_short_integral_literal3632 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_long_literal_in_integral_literal3647 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_integral_literal3659 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_short_literal_in_integral_literal3667 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_byte_literal_in_integral_literal3675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INTEGER_LITERAL_in_integer_literal3691 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONG_LITERAL_in_long_literal3706 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORT_LITERAL_in_short_literal3721 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BYTE_LITERAL_in_byte_literal3736 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_LITERAL_in_float_literal3751 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_LITERAL_in_double_literal3766 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_char_literal3781 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_string_literal3796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_LITERAL_in_bool_literal3815 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_I_ENCODED_ARRAY_in_array_literal3837 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_literal_in_array_literal3840 = new BitSet(new long[]{0x0000008000819908L,0x4001F00040000000L,0x0820000000000000L,0x0000000000222102L});
	public static final BitSet FOLLOW_I_ANNOTATIONS_in_annotations3865 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_annotation_in_annotations3868 = new BitSet(new long[]{0x0000000000000008L,0x0000000200000000L});
	public static final BitSet FOLLOW_I_ANNOTATION_in_annotation3897 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_ANNOTATION_VISIBILITY_in_annotation3899 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0020000000000000L});
	public static final BitSet FOLLOW_subannotation_in_annotation3901 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_ANNOTATION_ELEMENT_in_annotation_element3922 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_annotation_element3924 = new BitSet(new long[]{0x0000008000819900L,0x4001F00040000000L,0x0820000000000000L,0x0000000000222102L});
	public static final BitSet FOLLOW_literal_in_annotation_element3926 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_SUBANNOTATION_in_subannotation3953 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_subannotation3963 = new BitSet(new long[]{0x0000000000000008L,0x0000000800000000L});
	public static final BitSet FOLLOW_annotation_element_in_subannotation3974 = new BitSet(new long[]{0x0000000000000008L,0x0000000800000000L});
	public static final BitSet FOLLOW_I_ENCODED_FIELD_in_field_literal4013 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_field_reference_in_field_literal4015 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_ENCODED_METHOD_in_method_literal4036 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_method_reference_in_method_literal4038 = new BitSet(new long[]{0x0000000000000008L});
	public static final BitSet FOLLOW_I_ENCODED_ENUM_in_enum_literal4059 = new BitSet(new long[]{0x0000000000000004L});
	public static final BitSet FOLLOW_field_reference_in_enum_literal4061 = new BitSet(new long[]{0x0000000000000008L});
}
