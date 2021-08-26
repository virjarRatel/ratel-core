// $ANTLR 3.5.2 smaliParser.g 2019-01-23 00:20:01

package org.jf.smali;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class smaliParser extends Parser {
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
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public smaliParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public smaliParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return smaliParser.tokenNames; }
	@Override public String getGrammarFileName() { return "smaliParser.g"; }


	  public static final int ERROR_CHANNEL = 100;

	  private boolean verboseErrors = false;
	  private boolean allowOdex = false;
	  private int apiLevel = 15;
	  private Opcodes opcodes = Opcodes.forApi(apiLevel);

	  public void setVerboseErrors(boolean verboseErrors) {
	    this.verboseErrors = verboseErrors;
	  }

	  public void setAllowOdex(boolean allowOdex) {
	      this.allowOdex = allowOdex;
	  }

	  public void setApiLevel(int apiLevel) {
	      this.opcodes = Opcodes.forApi(apiLevel);
	      this.apiLevel = apiLevel;
	  }

	  public String getErrorMessage(RecognitionException e,
	    String[] tokenNames) {

	    if (verboseErrors) {
	      List stack = getRuleInvocationStack(e, this.getClass().getName());
	      String msg = null;

	      if (e instanceof NoViableAltException) {
	        NoViableAltException nvae = (NoViableAltException)e;
	        msg = " no viable alt; token="+getTokenErrorDisplay(e.token)+
	        " (decision="+nvae.decisionNumber+
	        " state "+nvae.stateNumber+")"+
	        " decision=<<"+nvae.grammarDecisionDescription+">>";
	      } else {
	        msg = super.getErrorMessage(e, tokenNames);
	      }

	      return stack + " " + msg;
	    } else {
	      return super.getErrorMessage(e, tokenNames);
	    }
	  }

	  public String getTokenErrorDisplay(Token t) {
	    if (!verboseErrors) {
	      String s = t.getText();
	      if ( s==null ) {
	        if ( t.getType()==Token.EOF ) {
	          s = "<EOF>";
	        }
	        else {
	          s = "<"+tokenNames[t.getType()]+">";
	        }
	      }
	      s = s.replaceAll("\n","\\\\n");
	      s = s.replaceAll("\r","\\\\r");
	      s = s.replaceAll("\t","\\\\t");
	      return "'"+s+"'";
	    }

	    CommonToken ct = (CommonToken)t;

	    String channelStr = "";
	    if (t.getChannel()>0) {
	      channelStr=",channel="+t.getChannel();
	    }
	    String txt = t.getText();
	    if ( txt!=null ) {
	      txt = txt.replaceAll("\n","\\\\n");
	      txt = txt.replaceAll("\r","\\\\r");
	      txt = txt.replaceAll("\t","\\\\t");
	    }
	    else {
	      txt = "<no text>";
	    }
	    return "[@"+t.getTokenIndex()+","+ct.getStartIndex()+":"+ct.getStopIndex()+"='"+txt+"',<"+tokenNames[t.getType()]+">"+channelStr+","+t.getLine()+":"+t.getCharPositionInLine()+"]";
	  }

	  public String getErrorHeader(RecognitionException e) {
	    return getSourceName()+"["+ e.line+","+e.charPositionInLine+"]";
	  }

	  private CommonTree buildTree(int type, String text, List<CommonTree> children) {
	    CommonTree root = new CommonTree(new CommonToken(type, text));
	    for (CommonTree child: children) {
	      root.addChild(child);
	    }
	    return root;
	  }

	  private CommonToken getParamListSubToken(CommonToken baseToken, String str, int typeStartIndex) {
	    CommonToken token = new CommonToken(baseToken);
	    token.setStartIndex(baseToken.getStartIndex() + typeStartIndex);

	    switch (str.charAt(typeStartIndex)) {
	      case 'Z':
	      case 'B':
	      case 'S':
	      case 'C':
	      case 'I':
	      case 'J':
	      case 'F':
	      case 'D':
	      {
	        token.setType(PRIMITIVE_TYPE);
	        token.setText(str.substring(typeStartIndex, typeStartIndex+1));
	        token.setStopIndex(baseToken.getStartIndex() + typeStartIndex);
	        break;
	      }
	      case 'L':
	      {
	        int i = typeStartIndex;
	        while (str.charAt(++i) != ';');

	        token.setType(CLASS_DESCRIPTOR);
	        token.setText(str.substring(typeStartIndex, i + 1));
	        token.setStopIndex(baseToken.getStartIndex() + i);
	        break;
	      }
	      case '[':
	      {
	        int i = typeStartIndex;
	        while (str.charAt(++i) == '[');

	        token.setType(ARRAY_TYPE_PREFIX);
	        token.setText(str.substring(typeStartIndex, i));
	        token.setStopIndex(baseToken.getStartIndex() + i - 1);
	        break;
	      }
	      default:
	        throw new RuntimeException(String.format("Invalid character '%c' in param list \"%s\" at position %d", str.charAt(typeStartIndex), str, typeStartIndex));
	    }

	    return token;
	  }

	  private CommonTree parseParamList(CommonToken paramListToken) {
	    String paramList = paramListToken.getText();
	    CommonTree root = new CommonTree();

	    int startIndex = paramListToken.getStartIndex();

	    int i=0;
	    while (i<paramList.length()) {
	      CommonToken token = getParamListSubToken(paramListToken, paramList, i);
	      root.addChild(new CommonTree(token));
	      i += token.getText().length();
	    }

	    if (root.getChildCount() == 0) {
	      return null;
	    }
	    return root;
	  }

	  private void throwOdexedInstructionException(IntStream input, String odexedInstruction)
	      throws OdexedInstructionException {
	    /*this has to be done in a separate method, otherwise java will complain about the
	    auto-generated code in the rule after the throw not being reachable*/
	    throw new OdexedInstructionException(input, odexedInstruction);
	  }


	protected static class smali_file_scope {
		boolean hasClassSpec;
		boolean hasSuperSpec;
		boolean hasSourceSpec;
		List<CommonTree> classAnnotations;
	}
	protected Stack<smali_file_scope> smali_file_stack = new Stack<smali_file_scope>();

	public static class smali_file_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "smali_file"
	// smaliParser.g:427:1: smali_file : ({...}? => class_spec |{...}? => super_spec | implements_spec |{...}? => source_spec | method | field | annotation )+ EOF -> ^( I_CLASS_DEF class_spec ( super_spec )? ( implements_spec )* ( source_spec )? ^( I_METHODS ( method )* ) ^( I_FIELDS ( field )* ) ) ;
	public final smaliParser.smali_file_return smali_file() throws RecognitionException {
		smali_file_stack.push(new smali_file_scope());
		smaliParser.smali_file_return retval = new smaliParser.smali_file_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EOF8=null;
		ParserRuleReturnScope class_spec1 =null;
		ParserRuleReturnScope super_spec2 =null;
		ParserRuleReturnScope implements_spec3 =null;
		ParserRuleReturnScope source_spec4 =null;
		ParserRuleReturnScope method5 =null;
		ParserRuleReturnScope field6 =null;
		ParserRuleReturnScope annotation7 =null;

		CommonTree EOF8_tree=null;
		RewriteRuleTokenStream stream_EOF=new RewriteRuleTokenStream(adaptor,"token EOF");
		RewriteRuleSubtreeStream stream_class_spec=new RewriteRuleSubtreeStream(adaptor,"rule class_spec");
		RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
		RewriteRuleSubtreeStream stream_method=new RewriteRuleSubtreeStream(adaptor,"rule method");
		RewriteRuleSubtreeStream stream_field=new RewriteRuleSubtreeStream(adaptor,"rule field");
		RewriteRuleSubtreeStream stream_super_spec=new RewriteRuleSubtreeStream(adaptor,"rule super_spec");
		RewriteRuleSubtreeStream stream_implements_spec=new RewriteRuleSubtreeStream(adaptor,"rule implements_spec");
		RewriteRuleSubtreeStream stream_source_spec=new RewriteRuleSubtreeStream(adaptor,"rule source_spec");

		 smali_file_stack.peek().hasClassSpec = smali_file_stack.peek().hasSuperSpec = smali_file_stack.peek().hasSourceSpec = false;
		    smali_file_stack.peek().classAnnotations = new ArrayList<CommonTree>();

		try {
			// smaliParser.g:439:3: ( ({...}? => class_spec |{...}? => super_spec | implements_spec |{...}? => source_spec | method | field | annotation )+ EOF -> ^( I_CLASS_DEF class_spec ( super_spec )? ( implements_spec )* ( source_spec )? ^( I_METHODS ( method )* ) ^( I_FIELDS ( field )* ) ) )
			// smaliParser.g:440:3: ({...}? => class_spec |{...}? => super_spec | implements_spec |{...}? => source_spec | method | field | annotation )+ EOF
			{
			// smaliParser.g:440:3: ({...}? => class_spec |{...}? => super_spec | implements_spec |{...}? => source_spec | method | field | annotation )+
			int cnt1=0;
			loop1:
			while (true) {
				int alt1=8;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==CLASS_DIRECTIVE) && ((!smali_file_stack.peek().hasClassSpec))) {
					alt1=1;
				}
				else if ( (LA1_0==SUPER_DIRECTIVE) && ((!smali_file_stack.peek().hasSuperSpec))) {
					alt1=2;
				}
				else if ( (LA1_0==IMPLEMENTS_DIRECTIVE) ) {
					alt1=3;
				}
				else if ( (LA1_0==SOURCE_DIRECTIVE) && ((!smali_file_stack.peek().hasSourceSpec))) {
					alt1=4;
				}
				else if ( (LA1_0==METHOD_DIRECTIVE) ) {
					alt1=5;
				}
				else if ( (LA1_0==FIELD_DIRECTIVE) ) {
					alt1=6;
				}
				else if ( (LA1_0==ANNOTATION_DIRECTIVE) ) {
					alt1=7;
				}

				switch (alt1) {
				case 1 :
					// smaliParser.g:440:5: {...}? => class_spec
					{
					if ( !((!smali_file_stack.peek().hasClassSpec)) ) {
						throw new FailedPredicateException(input, "smali_file", "!$smali_file::hasClassSpec");
					}
					pushFollow(FOLLOW_class_spec_in_smali_file1140);
					class_spec1=class_spec();
					state._fsp--;

					stream_class_spec.add(class_spec1.getTree());
					smali_file_stack.peek().hasClassSpec = true;
					}
					break;
				case 2 :
					// smaliParser.g:441:5: {...}? => super_spec
					{
					if ( !((!smali_file_stack.peek().hasSuperSpec)) ) {
						throw new FailedPredicateException(input, "smali_file", "!$smali_file::hasSuperSpec");
					}
					pushFollow(FOLLOW_super_spec_in_smali_file1151);
					super_spec2=super_spec();
					state._fsp--;

					stream_super_spec.add(super_spec2.getTree());
					smali_file_stack.peek().hasSuperSpec = true;
					}
					break;
				case 3 :
					// smaliParser.g:442:5: implements_spec
					{
					pushFollow(FOLLOW_implements_spec_in_smali_file1159);
					implements_spec3=implements_spec();
					state._fsp--;

					stream_implements_spec.add(implements_spec3.getTree());
					}
					break;
				case 4 :
					// smaliParser.g:443:5: {...}? => source_spec
					{
					if ( !((!smali_file_stack.peek().hasSourceSpec)) ) {
						throw new FailedPredicateException(input, "smali_file", "!$smali_file::hasSourceSpec");
					}
					pushFollow(FOLLOW_source_spec_in_smali_file1168);
					source_spec4=source_spec();
					state._fsp--;

					stream_source_spec.add(source_spec4.getTree());
					smali_file_stack.peek().hasSourceSpec = true;
					}
					break;
				case 5 :
					// smaliParser.g:444:5: method
					{
					pushFollow(FOLLOW_method_in_smali_file1176);
					method5=method();
					state._fsp--;

					stream_method.add(method5.getTree());
					}
					break;
				case 6 :
					// smaliParser.g:445:5: field
					{
					pushFollow(FOLLOW_field_in_smali_file1182);
					field6=field();
					state._fsp--;

					stream_field.add(field6.getTree());
					}
					break;
				case 7 :
					// smaliParser.g:446:5: annotation
					{
					pushFollow(FOLLOW_annotation_in_smali_file1188);
					annotation7=annotation();
					state._fsp--;

					stream_annotation.add(annotation7.getTree());
					smali_file_stack.peek().classAnnotations.add((annotation7!=null?((CommonTree)annotation7.getTree()):null));
					}
					break;

				default :
					if ( cnt1 >= 1 ) break loop1;
					EarlyExitException eee = new EarlyExitException(1, input);
					throw eee;
				}
				cnt1++;
			}

			EOF8=(Token)match(input,EOF,FOLLOW_EOF_in_smali_file1199);
			stream_EOF.add(EOF8);


			    if (!smali_file_stack.peek().hasClassSpec) {
			      throw new SemanticException(input, "The file must contain a .class directive");
			    }

			    if (!smali_file_stack.peek().hasSuperSpec) {
			      if (!(class_spec1!=null?((smaliParser.class_spec_return)class_spec1).className:null).equals("Ljava/lang/Object;")) {
			        throw new SemanticException(input, "The file must contain a .super directive");
			      }
			    }

			// AST REWRITE
			// elements: source_spec, method, super_spec, implements_spec, field, class_spec
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 460:3: -> ^( I_CLASS_DEF class_spec ( super_spec )? ( implements_spec )* ( source_spec )? ^( I_METHODS ( method )* ) ^( I_FIELDS ( field )* ) )
			{
				// smaliParser.g:460:6: ^( I_CLASS_DEF class_spec ( super_spec )? ( implements_spec )* ( source_spec )? ^( I_METHODS ( method )* ) ^( I_FIELDS ( field )* ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CLASS_DEF, "I_CLASS_DEF"), root_1);
				adaptor.addChild(root_1, stream_class_spec.nextTree());
				// smaliParser.g:462:8: ( super_spec )?
				if ( stream_super_spec.hasNext() ) {
					adaptor.addChild(root_1, stream_super_spec.nextTree());
				}
				stream_super_spec.reset();

				// smaliParser.g:463:8: ( implements_spec )*
				while ( stream_implements_spec.hasNext() ) {
					adaptor.addChild(root_1, stream_implements_spec.nextTree());
				}
				stream_implements_spec.reset();

				// smaliParser.g:464:8: ( source_spec )?
				if ( stream_source_spec.hasNext() ) {
					adaptor.addChild(root_1, stream_source_spec.nextTree());
				}
				stream_source_spec.reset();

				// smaliParser.g:465:8: ^( I_METHODS ( method )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_METHODS, "I_METHODS"), root_2);
				// smaliParser.g:465:20: ( method )*
				while ( stream_method.hasNext() ) {
					adaptor.addChild(root_2, stream_method.nextTree());
				}
				stream_method.reset();

				adaptor.addChild(root_1, root_2);
				}

				// smaliParser.g:465:29: ^( I_FIELDS ( field )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELDS, "I_FIELDS"), root_2);
				// smaliParser.g:465:40: ( field )*
				while ( stream_field.hasNext() ) {
					adaptor.addChild(root_2, stream_field.nextTree());
				}
				stream_field.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_1, buildTree(I_ANNOTATIONS, "I_ANNOTATIONS", smali_file_stack.peek().classAnnotations));
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
			smali_file_stack.pop();
		}
		return retval;
	}
	// $ANTLR end "smali_file"


	public static class class_spec_return extends ParserRuleReturnScope {
		public String className;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "class_spec"
	// smaliParser.g:467:1: class_spec returns [String className] : CLASS_DIRECTIVE access_list CLASS_DESCRIPTOR -> CLASS_DESCRIPTOR access_list ;
	public final smaliParser.class_spec_return class_spec() throws RecognitionException {
		smaliParser.class_spec_return retval = new smaliParser.class_spec_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CLASS_DIRECTIVE9=null;
		Token CLASS_DESCRIPTOR11=null;
		ParserRuleReturnScope access_list10 =null;

		CommonTree CLASS_DIRECTIVE9_tree=null;
		CommonTree CLASS_DESCRIPTOR11_tree=null;
		RewriteRuleTokenStream stream_CLASS_DESCRIPTOR=new RewriteRuleTokenStream(adaptor,"token CLASS_DESCRIPTOR");
		RewriteRuleTokenStream stream_CLASS_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token CLASS_DIRECTIVE");
		RewriteRuleSubtreeStream stream_access_list=new RewriteRuleSubtreeStream(adaptor,"rule access_list");

		try {
			// smaliParser.g:468:3: ( CLASS_DIRECTIVE access_list CLASS_DESCRIPTOR -> CLASS_DESCRIPTOR access_list )
			// smaliParser.g:468:5: CLASS_DIRECTIVE access_list CLASS_DESCRIPTOR
			{
			CLASS_DIRECTIVE9=(Token)match(input,CLASS_DIRECTIVE,FOLLOW_CLASS_DIRECTIVE_in_class_spec1286);
			stream_CLASS_DIRECTIVE.add(CLASS_DIRECTIVE9);

			pushFollow(FOLLOW_access_list_in_class_spec1288);
			access_list10=access_list();
			state._fsp--;

			stream_access_list.add(access_list10.getTree());
			CLASS_DESCRIPTOR11=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_class_spec1290);
			stream_CLASS_DESCRIPTOR.add(CLASS_DESCRIPTOR11);

			retval.className = (CLASS_DESCRIPTOR11!=null?CLASS_DESCRIPTOR11.getText():null);
			// AST REWRITE
			// elements: CLASS_DESCRIPTOR, access_list
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 468:89: -> CLASS_DESCRIPTOR access_list
			{
				adaptor.addChild(root_0, stream_CLASS_DESCRIPTOR.nextNode());
				adaptor.addChild(root_0, stream_access_list.nextTree());
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "class_spec"


	public static class super_spec_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "super_spec"
	// smaliParser.g:470:1: super_spec : SUPER_DIRECTIVE CLASS_DESCRIPTOR -> ^( I_SUPER[$start, \"I_SUPER\"] CLASS_DESCRIPTOR ) ;
	public final smaliParser.super_spec_return super_spec() throws RecognitionException {
		smaliParser.super_spec_return retval = new smaliParser.super_spec_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SUPER_DIRECTIVE12=null;
		Token CLASS_DESCRIPTOR13=null;

		CommonTree SUPER_DIRECTIVE12_tree=null;
		CommonTree CLASS_DESCRIPTOR13_tree=null;
		RewriteRuleTokenStream stream_CLASS_DESCRIPTOR=new RewriteRuleTokenStream(adaptor,"token CLASS_DESCRIPTOR");
		RewriteRuleTokenStream stream_SUPER_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token SUPER_DIRECTIVE");

		try {
			// smaliParser.g:471:3: ( SUPER_DIRECTIVE CLASS_DESCRIPTOR -> ^( I_SUPER[$start, \"I_SUPER\"] CLASS_DESCRIPTOR ) )
			// smaliParser.g:471:5: SUPER_DIRECTIVE CLASS_DESCRIPTOR
			{
			SUPER_DIRECTIVE12=(Token)match(input,SUPER_DIRECTIVE,FOLLOW_SUPER_DIRECTIVE_in_super_spec1308);
			stream_SUPER_DIRECTIVE.add(SUPER_DIRECTIVE12);

			CLASS_DESCRIPTOR13=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_super_spec1310);
			stream_CLASS_DESCRIPTOR.add(CLASS_DESCRIPTOR13);

			// AST REWRITE
			// elements: CLASS_DESCRIPTOR
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 471:38: -> ^( I_SUPER[$start, \"I_SUPER\"] CLASS_DESCRIPTOR )
			{
				// smaliParser.g:471:41: ^( I_SUPER[$start, \"I_SUPER\"] CLASS_DESCRIPTOR )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SUPER, (retval.start), "I_SUPER"), root_1);
				adaptor.addChild(root_1, stream_CLASS_DESCRIPTOR.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "super_spec"


	public static class implements_spec_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "implements_spec"
	// smaliParser.g:473:1: implements_spec : IMPLEMENTS_DIRECTIVE CLASS_DESCRIPTOR -> ^( I_IMPLEMENTS[$start, \"I_IMPLEMENTS\"] CLASS_DESCRIPTOR ) ;
	public final smaliParser.implements_spec_return implements_spec() throws RecognitionException {
		smaliParser.implements_spec_return retval = new smaliParser.implements_spec_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token IMPLEMENTS_DIRECTIVE14=null;
		Token CLASS_DESCRIPTOR15=null;

		CommonTree IMPLEMENTS_DIRECTIVE14_tree=null;
		CommonTree CLASS_DESCRIPTOR15_tree=null;
		RewriteRuleTokenStream stream_IMPLEMENTS_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token IMPLEMENTS_DIRECTIVE");
		RewriteRuleTokenStream stream_CLASS_DESCRIPTOR=new RewriteRuleTokenStream(adaptor,"token CLASS_DESCRIPTOR");

		try {
			// smaliParser.g:474:3: ( IMPLEMENTS_DIRECTIVE CLASS_DESCRIPTOR -> ^( I_IMPLEMENTS[$start, \"I_IMPLEMENTS\"] CLASS_DESCRIPTOR ) )
			// smaliParser.g:474:5: IMPLEMENTS_DIRECTIVE CLASS_DESCRIPTOR
			{
			IMPLEMENTS_DIRECTIVE14=(Token)match(input,IMPLEMENTS_DIRECTIVE,FOLLOW_IMPLEMENTS_DIRECTIVE_in_implements_spec1329);
			stream_IMPLEMENTS_DIRECTIVE.add(IMPLEMENTS_DIRECTIVE14);

			CLASS_DESCRIPTOR15=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_implements_spec1331);
			stream_CLASS_DESCRIPTOR.add(CLASS_DESCRIPTOR15);

			// AST REWRITE
			// elements: CLASS_DESCRIPTOR
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 474:43: -> ^( I_IMPLEMENTS[$start, \"I_IMPLEMENTS\"] CLASS_DESCRIPTOR )
			{
				// smaliParser.g:474:46: ^( I_IMPLEMENTS[$start, \"I_IMPLEMENTS\"] CLASS_DESCRIPTOR )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_IMPLEMENTS, (retval.start), "I_IMPLEMENTS"), root_1);
				adaptor.addChild(root_1, stream_CLASS_DESCRIPTOR.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "implements_spec"


	public static class source_spec_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "source_spec"
	// smaliParser.g:476:1: source_spec : SOURCE_DIRECTIVE STRING_LITERAL -> ^( I_SOURCE[$start, \"I_SOURCE\"] STRING_LITERAL ) ;
	public final smaliParser.source_spec_return source_spec() throws RecognitionException {
		smaliParser.source_spec_return retval = new smaliParser.source_spec_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SOURCE_DIRECTIVE16=null;
		Token STRING_LITERAL17=null;

		CommonTree SOURCE_DIRECTIVE16_tree=null;
		CommonTree STRING_LITERAL17_tree=null;
		RewriteRuleTokenStream stream_SOURCE_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token SOURCE_DIRECTIVE");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");

		try {
			// smaliParser.g:477:3: ( SOURCE_DIRECTIVE STRING_LITERAL -> ^( I_SOURCE[$start, \"I_SOURCE\"] STRING_LITERAL ) )
			// smaliParser.g:477:5: SOURCE_DIRECTIVE STRING_LITERAL
			{
			SOURCE_DIRECTIVE16=(Token)match(input,SOURCE_DIRECTIVE,FOLLOW_SOURCE_DIRECTIVE_in_source_spec1350);
			stream_SOURCE_DIRECTIVE.add(SOURCE_DIRECTIVE16);

			STRING_LITERAL17=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_source_spec1352);
			stream_STRING_LITERAL.add(STRING_LITERAL17);

			// AST REWRITE
			// elements: STRING_LITERAL
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 477:37: -> ^( I_SOURCE[$start, \"I_SOURCE\"] STRING_LITERAL )
			{
				// smaliParser.g:477:40: ^( I_SOURCE[$start, \"I_SOURCE\"] STRING_LITERAL )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SOURCE, (retval.start), "I_SOURCE"), root_1);
				adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "source_spec"


	public static class access_list_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "access_list"
	// smaliParser.g:479:1: access_list : ( ACCESS_SPEC )* -> ^( I_ACCESS_LIST[$start,\"I_ACCESS_LIST\"] ( ACCESS_SPEC )* ) ;
	public final smaliParser.access_list_return access_list() throws RecognitionException {
		smaliParser.access_list_return retval = new smaliParser.access_list_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ACCESS_SPEC18=null;

		CommonTree ACCESS_SPEC18_tree=null;
		RewriteRuleTokenStream stream_ACCESS_SPEC=new RewriteRuleTokenStream(adaptor,"token ACCESS_SPEC");

		try {
			// smaliParser.g:480:3: ( ( ACCESS_SPEC )* -> ^( I_ACCESS_LIST[$start,\"I_ACCESS_LIST\"] ( ACCESS_SPEC )* ) )
			// smaliParser.g:480:5: ( ACCESS_SPEC )*
			{
			// smaliParser.g:480:5: ( ACCESS_SPEC )*
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0==ACCESS_SPEC) ) {
					int LA2_2 = input.LA(2);
					if ( (LA2_2==ACCESS_SPEC||LA2_2==ANNOTATION_VISIBILITY||LA2_2==BOOL_LITERAL||LA2_2==CLASS_DESCRIPTOR||LA2_2==DOUBLE_LITERAL_OR_ID||LA2_2==FLOAT_LITERAL_OR_ID||(LA2_2 >= INSTRUCTION_FORMAT10t && LA2_2 <= INSTRUCTION_FORMAT10x_ODEX)||LA2_2==INSTRUCTION_FORMAT11x||LA2_2==INSTRUCTION_FORMAT12x_OR_ID||(LA2_2 >= INSTRUCTION_FORMAT21c_FIELD && LA2_2 <= INSTRUCTION_FORMAT21c_TYPE)||LA2_2==INSTRUCTION_FORMAT21t||(LA2_2 >= INSTRUCTION_FORMAT22c_FIELD && LA2_2 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA2_2 >= INSTRUCTION_FORMAT22s_OR_ID && LA2_2 <= INSTRUCTION_FORMAT22t)||LA2_2==INSTRUCTION_FORMAT23x||(LA2_2 >= INSTRUCTION_FORMAT31i_OR_ID && LA2_2 <= INSTRUCTION_FORMAT31t)||(LA2_2 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA2_2 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA2_2 >= INSTRUCTION_FORMAT45cc_METHOD && LA2_2 <= INSTRUCTION_FORMAT51l)||LA2_2==MEMBER_NAME||(LA2_2 >= METHOD_HANDLE_TYPE_FIELD && LA2_2 <= NULL_LITERAL)||(LA2_2 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA2_2 <= PRIMITIVE_TYPE)||LA2_2==REGISTER||LA2_2==SIMPLE_NAME||(LA2_2 >= VERIFICATION_ERROR_TYPE && LA2_2 <= VOID_TYPE)) ) {
						alt2=1;
					}

				}

				switch (alt2) {
				case 1 :
					// smaliParser.g:480:5: ACCESS_SPEC
					{
					ACCESS_SPEC18=(Token)match(input,ACCESS_SPEC,FOLLOW_ACCESS_SPEC_in_access_list1371);
					stream_ACCESS_SPEC.add(ACCESS_SPEC18);

					}
					break;

				default :
					break loop2;
				}
			}

			// AST REWRITE
			// elements: ACCESS_SPEC
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 480:18: -> ^( I_ACCESS_LIST[$start,\"I_ACCESS_LIST\"] ( ACCESS_SPEC )* )
			{
				// smaliParser.g:480:21: ^( I_ACCESS_LIST[$start,\"I_ACCESS_LIST\"] ( ACCESS_SPEC )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ACCESS_LIST, (retval.start), "I_ACCESS_LIST"), root_1);
				// smaliParser.g:480:61: ( ACCESS_SPEC )*
				while ( stream_ACCESS_SPEC.hasNext() ) {
					adaptor.addChild(root_1, stream_ACCESS_SPEC.nextNode());
				}
				stream_ACCESS_SPEC.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "access_list"


	public static class field_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "field"
	// smaliParser.g:487:1: field : FIELD_DIRECTIVE access_list member_name COLON nonvoid_type_descriptor ( EQUAL literal )? ( ({...}? annotation )* ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) ) ) ;
	public final smaliParser.field_return field() throws RecognitionException {
		smaliParser.field_return retval = new smaliParser.field_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FIELD_DIRECTIVE19=null;
		Token COLON22=null;
		Token EQUAL24=null;
		Token END_FIELD_DIRECTIVE27=null;
		ParserRuleReturnScope access_list20 =null;
		ParserRuleReturnScope member_name21 =null;
		ParserRuleReturnScope nonvoid_type_descriptor23 =null;
		ParserRuleReturnScope literal25 =null;
		ParserRuleReturnScope annotation26 =null;

		CommonTree FIELD_DIRECTIVE19_tree=null;
		CommonTree COLON22_tree=null;
		CommonTree EQUAL24_tree=null;
		CommonTree END_FIELD_DIRECTIVE27_tree=null;
		RewriteRuleTokenStream stream_END_FIELD_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_FIELD_DIRECTIVE");
		RewriteRuleTokenStream stream_EQUAL=new RewriteRuleTokenStream(adaptor,"token EQUAL");
		RewriteRuleTokenStream stream_FIELD_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token FIELD_DIRECTIVE");
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
		RewriteRuleSubtreeStream stream_access_list=new RewriteRuleSubtreeStream(adaptor,"rule access_list");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");
		RewriteRuleSubtreeStream stream_member_name=new RewriteRuleSubtreeStream(adaptor,"rule member_name");
		RewriteRuleSubtreeStream stream_literal=new RewriteRuleSubtreeStream(adaptor,"rule literal");

		List<CommonTree> annotations = new ArrayList<CommonTree>();
		try {
			// smaliParser.g:489:3: ( FIELD_DIRECTIVE access_list member_name COLON nonvoid_type_descriptor ( EQUAL literal )? ( ({...}? annotation )* ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) ) ) )
			// smaliParser.g:489:5: FIELD_DIRECTIVE access_list member_name COLON nonvoid_type_descriptor ( EQUAL literal )? ( ({...}? annotation )* ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) ) )
			{
			FIELD_DIRECTIVE19=(Token)match(input,FIELD_DIRECTIVE,FOLLOW_FIELD_DIRECTIVE_in_field1402);
			stream_FIELD_DIRECTIVE.add(FIELD_DIRECTIVE19);

			pushFollow(FOLLOW_access_list_in_field1404);
			access_list20=access_list();
			state._fsp--;

			stream_access_list.add(access_list20.getTree());
			pushFollow(FOLLOW_member_name_in_field1406);
			member_name21=member_name();
			state._fsp--;

			stream_member_name.add(member_name21.getTree());
			COLON22=(Token)match(input,COLON,FOLLOW_COLON_in_field1408);
			stream_COLON.add(COLON22);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_field1410);
			nonvoid_type_descriptor23=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor23.getTree());
			// smaliParser.g:489:75: ( EQUAL literal )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==EQUAL) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// smaliParser.g:489:76: EQUAL literal
					{
					EQUAL24=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_field1413);
					stream_EQUAL.add(EQUAL24);

					pushFollow(FOLLOW_literal_in_field1415);
					literal25=literal();
					state._fsp--;

					stream_literal.add(literal25.getTree());
					}
					break;

			}

			// smaliParser.g:490:5: ( ({...}? annotation )* ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) ) )
			// smaliParser.g:490:7: ({...}? annotation )* ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) )
			{
			// smaliParser.g:490:7: ({...}? annotation )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==ANNOTATION_DIRECTIVE) ) {
					int LA4_9 = input.LA(2);
					if ( ((input.LA(1) == ANNOTATION_DIRECTIVE)) ) {
						alt4=1;
					}

				}

				switch (alt4) {
				case 1 :
					// smaliParser.g:490:8: {...}? annotation
					{
					if ( !((input.LA(1) == ANNOTATION_DIRECTIVE)) ) {
						throw new FailedPredicateException(input, "field", "input.LA(1) == ANNOTATION_DIRECTIVE");
					}
					pushFollow(FOLLOW_annotation_in_field1428);
					annotation26=annotation();
					state._fsp--;

					stream_annotation.add(annotation26.getTree());
					annotations.add((annotation26!=null?((CommonTree)annotation26.getTree()):null));
					}
					break;

				default :
					break loop4;
				}
			}

			// smaliParser.g:491:7: ( END_FIELD_DIRECTIVE -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) ) )
			int alt5=2;
			int LA5_0 = input.LA(1);
			if ( (LA5_0==END_FIELD_DIRECTIVE) ) {
				alt5=1;
			}
			else if ( (LA5_0==EOF||LA5_0==ANNOTATION_DIRECTIVE||LA5_0==CLASS_DIRECTIVE||LA5_0==FIELD_DIRECTIVE||LA5_0==IMPLEMENTS_DIRECTIVE||LA5_0==METHOD_DIRECTIVE||LA5_0==SOURCE_DIRECTIVE||LA5_0==SUPER_DIRECTIVE) ) {
				alt5=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 5, 0, input);
				throw nvae;
			}

			switch (alt5) {
				case 1 :
					// smaliParser.g:491:9: END_FIELD_DIRECTIVE
					{
					END_FIELD_DIRECTIVE27=(Token)match(input,END_FIELD_DIRECTIVE,FOLLOW_END_FIELD_DIRECTIVE_in_field1442);
					stream_END_FIELD_DIRECTIVE.add(END_FIELD_DIRECTIVE27);

					// AST REWRITE
					// elements: annotation, nonvoid_type_descriptor, access_list, member_name, literal
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 492:9: -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) )
					{
						// smaliParser.g:492:12: ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ( annotation )* ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD, (retval.start), "I_FIELD"), root_1);
						adaptor.addChild(root_1, stream_member_name.nextTree());
						adaptor.addChild(root_1, stream_access_list.nextTree());
						// smaliParser.g:492:65: ^( I_FIELD_TYPE nonvoid_type_descriptor )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD_TYPE, "I_FIELD_TYPE"), root_2);
						adaptor.addChild(root_2, stream_nonvoid_type_descriptor.nextTree());
						adaptor.addChild(root_1, root_2);
						}

						// smaliParser.g:492:105: ( ^( I_FIELD_INITIAL_VALUE literal ) )?
						if ( stream_literal.hasNext() ) {
							// smaliParser.g:492:105: ^( I_FIELD_INITIAL_VALUE literal )
							{
							CommonTree root_2 = (CommonTree)adaptor.nil();
							root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD_INITIAL_VALUE, "I_FIELD_INITIAL_VALUE"), root_2);
							adaptor.addChild(root_2, stream_literal.nextTree());
							adaptor.addChild(root_1, root_2);
							}

						}
						stream_literal.reset();

						// smaliParser.g:492:139: ^( I_ANNOTATIONS ( annotation )* )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATIONS, "I_ANNOTATIONS"), root_2);
						// smaliParser.g:492:155: ( annotation )*
						while ( stream_annotation.hasNext() ) {
							adaptor.addChild(root_2, stream_annotation.nextTree());
						}
						stream_annotation.reset();

						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:493:21:
					{
					smali_file_stack.peek().classAnnotations.addAll(annotations);
					// AST REWRITE
					// elements: member_name, literal, nonvoid_type_descriptor, access_list
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 494:9: -> ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) )
					{
						// smaliParser.g:494:12: ^( I_FIELD[$start, \"I_FIELD\"] member_name access_list ^( I_FIELD_TYPE nonvoid_type_descriptor ) ( ^( I_FIELD_INITIAL_VALUE literal ) )? ^( I_ANNOTATIONS ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD, (retval.start), "I_FIELD"), root_1);
						adaptor.addChild(root_1, stream_member_name.nextTree());
						adaptor.addChild(root_1, stream_access_list.nextTree());
						// smaliParser.g:494:65: ^( I_FIELD_TYPE nonvoid_type_descriptor )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD_TYPE, "I_FIELD_TYPE"), root_2);
						adaptor.addChild(root_2, stream_nonvoid_type_descriptor.nextTree());
						adaptor.addChild(root_1, root_2);
						}

						// smaliParser.g:494:105: ( ^( I_FIELD_INITIAL_VALUE literal ) )?
						if ( stream_literal.hasNext() ) {
							// smaliParser.g:494:105: ^( I_FIELD_INITIAL_VALUE literal )
							{
							CommonTree root_2 = (CommonTree)adaptor.nil();
							root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_FIELD_INITIAL_VALUE, "I_FIELD_INITIAL_VALUE"), root_2);
							adaptor.addChild(root_2, stream_literal.nextTree());
							adaptor.addChild(root_1, root_2);
							}

						}
						stream_literal.reset();

						// smaliParser.g:494:139: ^( I_ANNOTATIONS )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATIONS, "I_ANNOTATIONS"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "field"


	public static class method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "method"
	// smaliParser.g:498:1: method : METHOD_DIRECTIVE access_list member_name method_prototype statements_and_directives END_METHOD_DIRECTIVE -> ^( I_METHOD[$start, \"I_METHOD\"] member_name method_prototype access_list statements_and_directives ) ;
	public final smaliParser.method_return method() throws RecognitionException {
		smaliParser.method_return retval = new smaliParser.method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token METHOD_DIRECTIVE28=null;
		Token END_METHOD_DIRECTIVE33=null;
		ParserRuleReturnScope access_list29 =null;
		ParserRuleReturnScope member_name30 =null;
		ParserRuleReturnScope method_prototype31 =null;
		ParserRuleReturnScope statements_and_directives32 =null;

		CommonTree METHOD_DIRECTIVE28_tree=null;
		CommonTree END_METHOD_DIRECTIVE33_tree=null;
		RewriteRuleTokenStream stream_END_METHOD_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_METHOD_DIRECTIVE");
		RewriteRuleTokenStream stream_METHOD_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token METHOD_DIRECTIVE");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_access_list=new RewriteRuleSubtreeStream(adaptor,"rule access_list");
		RewriteRuleSubtreeStream stream_member_name=new RewriteRuleSubtreeStream(adaptor,"rule member_name");
		RewriteRuleSubtreeStream stream_statements_and_directives=new RewriteRuleSubtreeStream(adaptor,"rule statements_and_directives");

		try {
			// smaliParser.g:499:3: ( METHOD_DIRECTIVE access_list member_name method_prototype statements_and_directives END_METHOD_DIRECTIVE -> ^( I_METHOD[$start, \"I_METHOD\"] member_name method_prototype access_list statements_and_directives ) )
			// smaliParser.g:499:5: METHOD_DIRECTIVE access_list member_name method_prototype statements_and_directives END_METHOD_DIRECTIVE
			{
			METHOD_DIRECTIVE28=(Token)match(input,METHOD_DIRECTIVE,FOLLOW_METHOD_DIRECTIVE_in_method1553);
			stream_METHOD_DIRECTIVE.add(METHOD_DIRECTIVE28);

			pushFollow(FOLLOW_access_list_in_method1555);
			access_list29=access_list();
			state._fsp--;

			stream_access_list.add(access_list29.getTree());
			pushFollow(FOLLOW_member_name_in_method1557);
			member_name30=member_name();
			state._fsp--;

			stream_member_name.add(member_name30.getTree());
			pushFollow(FOLLOW_method_prototype_in_method1559);
			method_prototype31=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype31.getTree());
			pushFollow(FOLLOW_statements_and_directives_in_method1561);
			statements_and_directives32=statements_and_directives();
			state._fsp--;

			stream_statements_and_directives.add(statements_and_directives32.getTree());
			END_METHOD_DIRECTIVE33=(Token)match(input,END_METHOD_DIRECTIVE,FOLLOW_END_METHOD_DIRECTIVE_in_method1567);
			stream_END_METHOD_DIRECTIVE.add(END_METHOD_DIRECTIVE33);

			// AST REWRITE
			// elements: method_prototype, statements_and_directives, access_list, member_name
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 501:5: -> ^( I_METHOD[$start, \"I_METHOD\"] member_name method_prototype access_list statements_and_directives )
			{
				// smaliParser.g:501:8: ^( I_METHOD[$start, \"I_METHOD\"] member_name method_prototype access_list statements_and_directives )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_METHOD, (retval.start), "I_METHOD"), root_1);
				adaptor.addChild(root_1, stream_member_name.nextTree());
				adaptor.addChild(root_1, stream_method_prototype.nextTree());
				adaptor.addChild(root_1, stream_access_list.nextTree());
				adaptor.addChild(root_1, stream_statements_and_directives.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method"


	protected static class statements_and_directives_scope {
		boolean hasRegistersDirective;
		List<CommonTree> methodAnnotations;
	}
	protected Stack<statements_and_directives_scope> statements_and_directives_stack = new Stack<statements_and_directives_scope>();

	public static class statements_and_directives_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "statements_and_directives"
	// smaliParser.g:503:1: statements_and_directives : ( ordered_method_item | registers_directive | catch_directive | catchall_directive | parameter_directive | annotation )* -> ( registers_directive )? ^( I_ORDERED_METHOD_ITEMS ( ordered_method_item )* ) ^( I_CATCHES ( catch_directive )* ( catchall_directive )* ) ^( I_PARAMETERS ( parameter_directive )* ) ;
	public final smaliParser.statements_and_directives_return statements_and_directives() throws RecognitionException {
		statements_and_directives_stack.push(new statements_and_directives_scope());
		smaliParser.statements_and_directives_return retval = new smaliParser.statements_and_directives_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope ordered_method_item34 =null;
		ParserRuleReturnScope registers_directive35 =null;
		ParserRuleReturnScope catch_directive36 =null;
		ParserRuleReturnScope catchall_directive37 =null;
		ParserRuleReturnScope parameter_directive38 =null;
		ParserRuleReturnScope annotation39 =null;

		RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");
		RewriteRuleSubtreeStream stream_catchall_directive=new RewriteRuleSubtreeStream(adaptor,"rule catchall_directive");
		RewriteRuleSubtreeStream stream_registers_directive=new RewriteRuleSubtreeStream(adaptor,"rule registers_directive");
		RewriteRuleSubtreeStream stream_catch_directive=new RewriteRuleSubtreeStream(adaptor,"rule catch_directive");
		RewriteRuleSubtreeStream stream_ordered_method_item=new RewriteRuleSubtreeStream(adaptor,"rule ordered_method_item");
		RewriteRuleSubtreeStream stream_parameter_directive=new RewriteRuleSubtreeStream(adaptor,"rule parameter_directive");

		try {
			// smaliParser.g:509:3: ( ( ordered_method_item | registers_directive | catch_directive | catchall_directive | parameter_directive | annotation )* -> ( registers_directive )? ^( I_ORDERED_METHOD_ITEMS ( ordered_method_item )* ) ^( I_CATCHES ( catch_directive )* ( catchall_directive )* ) ^( I_PARAMETERS ( parameter_directive )* ) )
			// smaliParser.g:509:5: ( ordered_method_item | registers_directive | catch_directive | catchall_directive | parameter_directive | annotation )*
			{

			      statements_and_directives_stack.peek().hasRegistersDirective = false;
			      statements_and_directives_stack.peek().methodAnnotations = new ArrayList<CommonTree>();

			// smaliParser.g:513:5: ( ordered_method_item | registers_directive | catch_directive | catchall_directive | parameter_directive | annotation )*
			loop6:
			while (true) {
				int alt6=7;
				switch ( input.LA(1) ) {
				case ARRAY_DATA_DIRECTIVE:
				case COLON:
				case END_LOCAL_DIRECTIVE:
				case EPILOGUE_DIRECTIVE:
				case INSTRUCTION_FORMAT10t:
				case INSTRUCTION_FORMAT10x:
				case INSTRUCTION_FORMAT10x_ODEX:
				case INSTRUCTION_FORMAT11n:
				case INSTRUCTION_FORMAT11x:
				case INSTRUCTION_FORMAT12x:
				case INSTRUCTION_FORMAT12x_OR_ID:
				case INSTRUCTION_FORMAT20bc:
				case INSTRUCTION_FORMAT20t:
				case INSTRUCTION_FORMAT21c_FIELD:
				case INSTRUCTION_FORMAT21c_FIELD_ODEX:
				case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
				case INSTRUCTION_FORMAT21c_METHOD_TYPE:
				case INSTRUCTION_FORMAT21c_STRING:
				case INSTRUCTION_FORMAT21c_TYPE:
				case INSTRUCTION_FORMAT21ih:
				case INSTRUCTION_FORMAT21lh:
				case INSTRUCTION_FORMAT21s:
				case INSTRUCTION_FORMAT21t:
				case INSTRUCTION_FORMAT22b:
				case INSTRUCTION_FORMAT22c_FIELD:
				case INSTRUCTION_FORMAT22c_FIELD_ODEX:
				case INSTRUCTION_FORMAT22c_TYPE:
				case INSTRUCTION_FORMAT22cs_FIELD:
				case INSTRUCTION_FORMAT22s:
				case INSTRUCTION_FORMAT22s_OR_ID:
				case INSTRUCTION_FORMAT22t:
				case INSTRUCTION_FORMAT22x:
				case INSTRUCTION_FORMAT23x:
				case INSTRUCTION_FORMAT30t:
				case INSTRUCTION_FORMAT31c:
				case INSTRUCTION_FORMAT31i:
				case INSTRUCTION_FORMAT31i_OR_ID:
				case INSTRUCTION_FORMAT31t:
				case INSTRUCTION_FORMAT32x:
				case INSTRUCTION_FORMAT35c_CALL_SITE:
				case INSTRUCTION_FORMAT35c_METHOD:
				case INSTRUCTION_FORMAT35c_METHOD_ODEX:
				case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
				case INSTRUCTION_FORMAT35c_TYPE:
				case INSTRUCTION_FORMAT35mi_METHOD:
				case INSTRUCTION_FORMAT35ms_METHOD:
				case INSTRUCTION_FORMAT3rc_CALL_SITE:
				case INSTRUCTION_FORMAT3rc_METHOD:
				case INSTRUCTION_FORMAT3rc_METHOD_ODEX:
				case INSTRUCTION_FORMAT3rc_TYPE:
				case INSTRUCTION_FORMAT3rmi_METHOD:
				case INSTRUCTION_FORMAT3rms_METHOD:
				case INSTRUCTION_FORMAT45cc_METHOD:
				case INSTRUCTION_FORMAT4rcc_METHOD:
				case INSTRUCTION_FORMAT51l:
				case LINE_DIRECTIVE:
				case LOCAL_DIRECTIVE:
				case PACKED_SWITCH_DIRECTIVE:
				case PROLOGUE_DIRECTIVE:
				case RESTART_LOCAL_DIRECTIVE:
				case SOURCE_DIRECTIVE:
				case SPARSE_SWITCH_DIRECTIVE:
					{
					alt6=1;
					}
					break;
				case LOCALS_DIRECTIVE:
				case REGISTERS_DIRECTIVE:
					{
					alt6=2;
					}
					break;
				case CATCH_DIRECTIVE:
					{
					alt6=3;
					}
					break;
				case CATCHALL_DIRECTIVE:
					{
					alt6=4;
					}
					break;
				case PARAMETER_DIRECTIVE:
					{
					alt6=5;
					}
					break;
				case ANNOTATION_DIRECTIVE:
					{
					alt6=6;
					}
					break;
				}
				switch (alt6) {
				case 1 :
					// smaliParser.g:513:7: ordered_method_item
					{
					pushFollow(FOLLOW_ordered_method_item_in_statements_and_directives1612);
					ordered_method_item34=ordered_method_item();
					state._fsp--;

					stream_ordered_method_item.add(ordered_method_item34.getTree());
					}
					break;
				case 2 :
					// smaliParser.g:514:7: registers_directive
					{
					pushFollow(FOLLOW_registers_directive_in_statements_and_directives1620);
					registers_directive35=registers_directive();
					state._fsp--;

					stream_registers_directive.add(registers_directive35.getTree());
					}
					break;
				case 3 :
					// smaliParser.g:515:7: catch_directive
					{
					pushFollow(FOLLOW_catch_directive_in_statements_and_directives1628);
					catch_directive36=catch_directive();
					state._fsp--;

					stream_catch_directive.add(catch_directive36.getTree());
					}
					break;
				case 4 :
					// smaliParser.g:516:7: catchall_directive
					{
					pushFollow(FOLLOW_catchall_directive_in_statements_and_directives1636);
					catchall_directive37=catchall_directive();
					state._fsp--;

					stream_catchall_directive.add(catchall_directive37.getTree());
					}
					break;
				case 5 :
					// smaliParser.g:517:7: parameter_directive
					{
					pushFollow(FOLLOW_parameter_directive_in_statements_and_directives1644);
					parameter_directive38=parameter_directive();
					state._fsp--;

					stream_parameter_directive.add(parameter_directive38.getTree());
					}
					break;
				case 6 :
					// smaliParser.g:518:7: annotation
					{
					pushFollow(FOLLOW_annotation_in_statements_and_directives1652);
					annotation39=annotation();
					state._fsp--;

					stream_annotation.add(annotation39.getTree());
					statements_and_directives_stack.peek().methodAnnotations.add((annotation39!=null?((CommonTree)annotation39.getTree()):null));
					}
					break;

				default :
					break loop6;
				}
			}

			// AST REWRITE
			// elements: parameter_directive, catchall_directive, ordered_method_item, catch_directive, registers_directive
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 520:5: -> ( registers_directive )? ^( I_ORDERED_METHOD_ITEMS ( ordered_method_item )* ) ^( I_CATCHES ( catch_directive )* ( catchall_directive )* ) ^( I_PARAMETERS ( parameter_directive )* )
			{
				// smaliParser.g:520:8: ( registers_directive )?
				if ( stream_registers_directive.hasNext() ) {
					adaptor.addChild(root_0, stream_registers_directive.nextTree());
				}
				stream_registers_directive.reset();

				// smaliParser.g:521:8: ^( I_ORDERED_METHOD_ITEMS ( ordered_method_item )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ORDERED_METHOD_ITEMS, "I_ORDERED_METHOD_ITEMS"), root_1);
				// smaliParser.g:521:33: ( ordered_method_item )*
				while ( stream_ordered_method_item.hasNext() ) {
					adaptor.addChild(root_1, stream_ordered_method_item.nextTree());
				}
				stream_ordered_method_item.reset();

				adaptor.addChild(root_0, root_1);
				}

				// smaliParser.g:522:8: ^( I_CATCHES ( catch_directive )* ( catchall_directive )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CATCHES, "I_CATCHES"), root_1);
				// smaliParser.g:522:20: ( catch_directive )*
				while ( stream_catch_directive.hasNext() ) {
					adaptor.addChild(root_1, stream_catch_directive.nextTree());
				}
				stream_catch_directive.reset();

				// smaliParser.g:522:37: ( catchall_directive )*
				while ( stream_catchall_directive.hasNext() ) {
					adaptor.addChild(root_1, stream_catchall_directive.nextTree());
				}
				stream_catchall_directive.reset();

				adaptor.addChild(root_0, root_1);
				}

				// smaliParser.g:523:8: ^( I_PARAMETERS ( parameter_directive )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PARAMETERS, "I_PARAMETERS"), root_1);
				// smaliParser.g:523:23: ( parameter_directive )*
				while ( stream_parameter_directive.hasNext() ) {
					adaptor.addChild(root_1, stream_parameter_directive.nextTree());
				}
				stream_parameter_directive.reset();

				adaptor.addChild(root_0, root_1);
				}

				adaptor.addChild(root_0, buildTree(I_ANNOTATIONS, "I_ANNOTATIONS", statements_and_directives_stack.peek().methodAnnotations));
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
			statements_and_directives_stack.pop();
		}
		return retval;
	}
	// $ANTLR end "statements_and_directives"


	public static class ordered_method_item_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "ordered_method_item"
	// smaliParser.g:527:1: ordered_method_item : ( label | instruction | debug_directive );
	public final smaliParser.ordered_method_item_return ordered_method_item() throws RecognitionException {
		smaliParser.ordered_method_item_return retval = new smaliParser.ordered_method_item_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope label40 =null;
		ParserRuleReturnScope instruction41 =null;
		ParserRuleReturnScope debug_directive42 =null;


		try {
			// smaliParser.g:528:3: ( label | instruction | debug_directive )
			int alt7=3;
			switch ( input.LA(1) ) {
			case COLON:
				{
				alt7=1;
				}
				break;
			case ARRAY_DATA_DIRECTIVE:
			case INSTRUCTION_FORMAT10t:
			case INSTRUCTION_FORMAT10x:
			case INSTRUCTION_FORMAT10x_ODEX:
			case INSTRUCTION_FORMAT11n:
			case INSTRUCTION_FORMAT11x:
			case INSTRUCTION_FORMAT12x:
			case INSTRUCTION_FORMAT12x_OR_ID:
			case INSTRUCTION_FORMAT20bc:
			case INSTRUCTION_FORMAT20t:
			case INSTRUCTION_FORMAT21c_FIELD:
			case INSTRUCTION_FORMAT21c_FIELD_ODEX:
			case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
			case INSTRUCTION_FORMAT21c_METHOD_TYPE:
			case INSTRUCTION_FORMAT21c_STRING:
			case INSTRUCTION_FORMAT21c_TYPE:
			case INSTRUCTION_FORMAT21ih:
			case INSTRUCTION_FORMAT21lh:
			case INSTRUCTION_FORMAT21s:
			case INSTRUCTION_FORMAT21t:
			case INSTRUCTION_FORMAT22b:
			case INSTRUCTION_FORMAT22c_FIELD:
			case INSTRUCTION_FORMAT22c_FIELD_ODEX:
			case INSTRUCTION_FORMAT22c_TYPE:
			case INSTRUCTION_FORMAT22cs_FIELD:
			case INSTRUCTION_FORMAT22s:
			case INSTRUCTION_FORMAT22s_OR_ID:
			case INSTRUCTION_FORMAT22t:
			case INSTRUCTION_FORMAT22x:
			case INSTRUCTION_FORMAT23x:
			case INSTRUCTION_FORMAT30t:
			case INSTRUCTION_FORMAT31c:
			case INSTRUCTION_FORMAT31i:
			case INSTRUCTION_FORMAT31i_OR_ID:
			case INSTRUCTION_FORMAT31t:
			case INSTRUCTION_FORMAT32x:
			case INSTRUCTION_FORMAT35c_CALL_SITE:
			case INSTRUCTION_FORMAT35c_METHOD:
			case INSTRUCTION_FORMAT35c_METHOD_ODEX:
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
			case INSTRUCTION_FORMAT35c_TYPE:
			case INSTRUCTION_FORMAT35mi_METHOD:
			case INSTRUCTION_FORMAT35ms_METHOD:
			case INSTRUCTION_FORMAT3rc_CALL_SITE:
			case INSTRUCTION_FORMAT3rc_METHOD:
			case INSTRUCTION_FORMAT3rc_METHOD_ODEX:
			case INSTRUCTION_FORMAT3rc_TYPE:
			case INSTRUCTION_FORMAT3rmi_METHOD:
			case INSTRUCTION_FORMAT3rms_METHOD:
			case INSTRUCTION_FORMAT45cc_METHOD:
			case INSTRUCTION_FORMAT4rcc_METHOD:
			case INSTRUCTION_FORMAT51l:
			case PACKED_SWITCH_DIRECTIVE:
			case SPARSE_SWITCH_DIRECTIVE:
				{
				alt7=2;
				}
				break;
			case END_LOCAL_DIRECTIVE:
			case EPILOGUE_DIRECTIVE:
			case LINE_DIRECTIVE:
			case LOCAL_DIRECTIVE:
			case PROLOGUE_DIRECTIVE:
			case RESTART_LOCAL_DIRECTIVE:
			case SOURCE_DIRECTIVE:
				{
				alt7=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}
			switch (alt7) {
				case 1 :
					// smaliParser.g:528:5: label
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_label_in_ordered_method_item1737);
					label40=label();
					state._fsp--;

					adaptor.addChild(root_0, label40.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:529:5: instruction
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_instruction_in_ordered_method_item1743);
					instruction41=instruction();
					state._fsp--;

					adaptor.addChild(root_0, instruction41.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:530:5: debug_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_debug_directive_in_ordered_method_item1749);
					debug_directive42=debug_directive();
					state._fsp--;

					adaptor.addChild(root_0, debug_directive42.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "ordered_method_item"


	public static class registers_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "registers_directive"
	// smaliParser.g:532:1: registers_directive : (directive= REGISTERS_DIRECTIVE regCount= integral_literal -> ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount) |directive= LOCALS_DIRECTIVE regCount2= integral_literal -> ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2) ) ;
	public final smaliParser.registers_directive_return registers_directive() throws RecognitionException {
		smaliParser.registers_directive_return retval = new smaliParser.registers_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token directive=null;
		ParserRuleReturnScope regCount =null;
		ParserRuleReturnScope regCount2 =null;

		CommonTree directive_tree=null;
		RewriteRuleTokenStream stream_LOCALS_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token LOCALS_DIRECTIVE");
		RewriteRuleTokenStream stream_REGISTERS_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token REGISTERS_DIRECTIVE");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:533:3: ( (directive= REGISTERS_DIRECTIVE regCount= integral_literal -> ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount) |directive= LOCALS_DIRECTIVE regCount2= integral_literal -> ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2) ) )
			// smaliParser.g:533:5: (directive= REGISTERS_DIRECTIVE regCount= integral_literal -> ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount) |directive= LOCALS_DIRECTIVE regCount2= integral_literal -> ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2) )
			{
			// smaliParser.g:533:5: (directive= REGISTERS_DIRECTIVE regCount= integral_literal -> ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount) |directive= LOCALS_DIRECTIVE regCount2= integral_literal -> ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2) )
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==REGISTERS_DIRECTIVE) ) {
				alt8=1;
			}
			else if ( (LA8_0==LOCALS_DIRECTIVE) ) {
				alt8=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// smaliParser.g:534:7: directive= REGISTERS_DIRECTIVE regCount= integral_literal
					{
					directive=(Token)match(input,REGISTERS_DIRECTIVE,FOLLOW_REGISTERS_DIRECTIVE_in_registers_directive1769);
					stream_REGISTERS_DIRECTIVE.add(directive);

					pushFollow(FOLLOW_integral_literal_in_registers_directive1773);
					regCount=integral_literal();
					state._fsp--;

					stream_integral_literal.add(regCount.getTree());
					// AST REWRITE
					// elements: regCount
					// token labels:
					// rule labels: regCount, retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_regCount=new RewriteRuleSubtreeStream(adaptor,"rule regCount",regCount!=null?regCount.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 534:63: -> ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount)
					{
						// smaliParser.g:534:66: ^( I_REGISTERS[$REGISTERS_DIRECTIVE, \"I_REGISTERS\"] $regCount)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_REGISTERS, directive, "I_REGISTERS"), root_1);
						adaptor.addChild(root_1, stream_regCount.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:535:7: directive= LOCALS_DIRECTIVE regCount2= integral_literal
					{
					directive=(Token)match(input,LOCALS_DIRECTIVE,FOLLOW_LOCALS_DIRECTIVE_in_registers_directive1793);
					stream_LOCALS_DIRECTIVE.add(directive);

					pushFollow(FOLLOW_integral_literal_in_registers_directive1797);
					regCount2=integral_literal();
					state._fsp--;

					stream_integral_literal.add(regCount2.getTree());
					// AST REWRITE
					// elements: regCount2
					// token labels:
					// rule labels: regCount2, retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_regCount2=new RewriteRuleSubtreeStream(adaptor,"rule regCount2",regCount2!=null?regCount2.getTree():null);
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 535:61: -> ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2)
					{
						// smaliParser.g:535:64: ^( I_LOCALS[$LOCALS_DIRECTIVE, \"I_LOCALS\"] $regCount2)
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_LOCALS, directive, "I_LOCALS"), root_1);
						adaptor.addChild(root_1, stream_regCount2.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}


			      if (statements_and_directives_stack.peek().hasRegistersDirective) {
			        throw new SemanticException(input, directive, "There can only be a single .registers or .locals directive in a method");
			      }
			      statements_and_directives_stack.peek().hasRegistersDirective =true;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "registers_directive"


	public static class param_list_or_id_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "param_list_or_id"
	// smaliParser.g:544:1: param_list_or_id : ( PARAM_LIST_OR_ID_PRIMITIVE_TYPE )+ ;
	public final smaliParser.param_list_or_id_return param_list_or_id() throws RecognitionException {
		smaliParser.param_list_or_id_return retval = new smaliParser.param_list_or_id_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PARAM_LIST_OR_ID_PRIMITIVE_TYPE43=null;

		CommonTree PARAM_LIST_OR_ID_PRIMITIVE_TYPE43_tree=null;

		try {
			// smaliParser.g:545:3: ( ( PARAM_LIST_OR_ID_PRIMITIVE_TYPE )+ )
			// smaliParser.g:545:5: ( PARAM_LIST_OR_ID_PRIMITIVE_TYPE )+
			{
			root_0 = (CommonTree)adaptor.nil();


			// smaliParser.g:545:5: ( PARAM_LIST_OR_ID_PRIMITIVE_TYPE )+
			int cnt9=0;
			loop9:
			while (true) {
				int alt9=2;
				int LA9_0 = input.LA(1);
				if ( (LA9_0==PARAM_LIST_OR_ID_PRIMITIVE_TYPE) ) {
					alt9=1;
				}

				switch (alt9) {
				case 1 :
					// smaliParser.g:545:5: PARAM_LIST_OR_ID_PRIMITIVE_TYPE
					{
					PARAM_LIST_OR_ID_PRIMITIVE_TYPE43=(Token)match(input,PARAM_LIST_OR_ID_PRIMITIVE_TYPE,FOLLOW_PARAM_LIST_OR_ID_PRIMITIVE_TYPE_in_param_list_or_id1829);
					PARAM_LIST_OR_ID_PRIMITIVE_TYPE43_tree = (CommonTree)adaptor.create(PARAM_LIST_OR_ID_PRIMITIVE_TYPE43);
					adaptor.addChild(root_0, PARAM_LIST_OR_ID_PRIMITIVE_TYPE43_tree);

					}
					break;

				default :
					if ( cnt9 >= 1 ) break loop9;
					EarlyExitException eee = new EarlyExitException(9, input);
					throw eee;
				}
				cnt9++;
			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "param_list_or_id"


	public static class simple_name_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "simple_name"
	// smaliParser.g:549:1: simple_name : ( SIMPLE_NAME | ACCESS_SPEC -> SIMPLE_NAME[$ACCESS_SPEC] | VERIFICATION_ERROR_TYPE -> SIMPLE_NAME[$VERIFICATION_ERROR_TYPE] | POSITIVE_INTEGER_LITERAL -> SIMPLE_NAME[$POSITIVE_INTEGER_LITERAL] | NEGATIVE_INTEGER_LITERAL -> SIMPLE_NAME[$NEGATIVE_INTEGER_LITERAL] | FLOAT_LITERAL_OR_ID -> SIMPLE_NAME[$FLOAT_LITERAL_OR_ID] | DOUBLE_LITERAL_OR_ID -> SIMPLE_NAME[$DOUBLE_LITERAL_OR_ID] | BOOL_LITERAL -> SIMPLE_NAME[$BOOL_LITERAL] | NULL_LITERAL -> SIMPLE_NAME[$NULL_LITERAL] | REGISTER -> SIMPLE_NAME[$REGISTER] | param_list_or_id ->| PRIMITIVE_TYPE -> SIMPLE_NAME[$PRIMITIVE_TYPE] | VOID_TYPE -> SIMPLE_NAME[$VOID_TYPE] | ANNOTATION_VISIBILITY -> SIMPLE_NAME[$ANNOTATION_VISIBILITY] | METHOD_HANDLE_TYPE_FIELD | METHOD_HANDLE_TYPE_METHOD | INSTRUCTION_FORMAT10t -> SIMPLE_NAME[$INSTRUCTION_FORMAT10t] | INSTRUCTION_FORMAT10x -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x] | INSTRUCTION_FORMAT10x_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x_ODEX] | INSTRUCTION_FORMAT11x -> SIMPLE_NAME[$INSTRUCTION_FORMAT11x] | INSTRUCTION_FORMAT12x_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT12x_OR_ID] | INSTRUCTION_FORMAT21c_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD] | INSTRUCTION_FORMAT21c_FIELD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD_ODEX] | INSTRUCTION_FORMAT21c_METHOD_HANDLE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_HANDLE] | INSTRUCTION_FORMAT21c_METHOD_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_TYPE] | INSTRUCTION_FORMAT21c_STRING -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_STRING] | INSTRUCTION_FORMAT21c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_TYPE] | INSTRUCTION_FORMAT21t -> SIMPLE_NAME[$INSTRUCTION_FORMAT21t] | INSTRUCTION_FORMAT22c_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD] | INSTRUCTION_FORMAT22c_FIELD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD_ODEX] | INSTRUCTION_FORMAT22c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_TYPE] | INSTRUCTION_FORMAT22cs_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT22cs_FIELD] | INSTRUCTION_FORMAT22s_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT22s_OR_ID] | INSTRUCTION_FORMAT22t -> SIMPLE_NAME[$INSTRUCTION_FORMAT22t] | INSTRUCTION_FORMAT23x -> SIMPLE_NAME[$INSTRUCTION_FORMAT23x] | INSTRUCTION_FORMAT31i_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT31i_OR_ID] | INSTRUCTION_FORMAT31t -> SIMPLE_NAME[$INSTRUCTION_FORMAT31t] | INSTRUCTION_FORMAT35c_CALL_SITE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_CALL_SITE] | INSTRUCTION_FORMAT35c_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD] | INSTRUCTION_FORMAT35c_METHOD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_ODEX] | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE] | INSTRUCTION_FORMAT35c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_TYPE] | INSTRUCTION_FORMAT35mi_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35mi_METHOD] | INSTRUCTION_FORMAT35ms_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35ms_METHOD] | INSTRUCTION_FORMAT45cc_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT45cc_METHOD] | INSTRUCTION_FORMAT4rcc_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT4rcc_METHOD] | INSTRUCTION_FORMAT51l -> SIMPLE_NAME[$INSTRUCTION_FORMAT51l] );
	public final smaliParser.simple_name_return simple_name() throws RecognitionException {
		smaliParser.simple_name_return retval = new smaliParser.simple_name_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SIMPLE_NAME44=null;
		Token ACCESS_SPEC45=null;
		Token VERIFICATION_ERROR_TYPE46=null;
		Token POSITIVE_INTEGER_LITERAL47=null;
		Token NEGATIVE_INTEGER_LITERAL48=null;
		Token FLOAT_LITERAL_OR_ID49=null;
		Token DOUBLE_LITERAL_OR_ID50=null;
		Token BOOL_LITERAL51=null;
		Token NULL_LITERAL52=null;
		Token REGISTER53=null;
		Token PRIMITIVE_TYPE55=null;
		Token VOID_TYPE56=null;
		Token ANNOTATION_VISIBILITY57=null;
		Token METHOD_HANDLE_TYPE_FIELD58=null;
		Token METHOD_HANDLE_TYPE_METHOD59=null;
		Token INSTRUCTION_FORMAT10t60=null;
		Token INSTRUCTION_FORMAT10x61=null;
		Token INSTRUCTION_FORMAT10x_ODEX62=null;
		Token INSTRUCTION_FORMAT11x63=null;
		Token INSTRUCTION_FORMAT12x_OR_ID64=null;
		Token INSTRUCTION_FORMAT21c_FIELD65=null;
		Token INSTRUCTION_FORMAT21c_FIELD_ODEX66=null;
		Token INSTRUCTION_FORMAT21c_METHOD_HANDLE67=null;
		Token INSTRUCTION_FORMAT21c_METHOD_TYPE68=null;
		Token INSTRUCTION_FORMAT21c_STRING69=null;
		Token INSTRUCTION_FORMAT21c_TYPE70=null;
		Token INSTRUCTION_FORMAT21t71=null;
		Token INSTRUCTION_FORMAT22c_FIELD72=null;
		Token INSTRUCTION_FORMAT22c_FIELD_ODEX73=null;
		Token INSTRUCTION_FORMAT22c_TYPE74=null;
		Token INSTRUCTION_FORMAT22cs_FIELD75=null;
		Token INSTRUCTION_FORMAT22s_OR_ID76=null;
		Token INSTRUCTION_FORMAT22t77=null;
		Token INSTRUCTION_FORMAT23x78=null;
		Token INSTRUCTION_FORMAT31i_OR_ID79=null;
		Token INSTRUCTION_FORMAT31t80=null;
		Token INSTRUCTION_FORMAT35c_CALL_SITE81=null;
		Token INSTRUCTION_FORMAT35c_METHOD82=null;
		Token INSTRUCTION_FORMAT35c_METHOD_ODEX83=null;
		Token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE84=null;
		Token INSTRUCTION_FORMAT35c_TYPE85=null;
		Token INSTRUCTION_FORMAT35mi_METHOD86=null;
		Token INSTRUCTION_FORMAT35ms_METHOD87=null;
		Token INSTRUCTION_FORMAT45cc_METHOD88=null;
		Token INSTRUCTION_FORMAT4rcc_METHOD89=null;
		Token INSTRUCTION_FORMAT51l90=null;
		ParserRuleReturnScope param_list_or_id54 =null;

		CommonTree SIMPLE_NAME44_tree=null;
		CommonTree ACCESS_SPEC45_tree=null;
		CommonTree VERIFICATION_ERROR_TYPE46_tree=null;
		CommonTree POSITIVE_INTEGER_LITERAL47_tree=null;
		CommonTree NEGATIVE_INTEGER_LITERAL48_tree=null;
		CommonTree FLOAT_LITERAL_OR_ID49_tree=null;
		CommonTree DOUBLE_LITERAL_OR_ID50_tree=null;
		CommonTree BOOL_LITERAL51_tree=null;
		CommonTree NULL_LITERAL52_tree=null;
		CommonTree REGISTER53_tree=null;
		CommonTree PRIMITIVE_TYPE55_tree=null;
		CommonTree VOID_TYPE56_tree=null;
		CommonTree ANNOTATION_VISIBILITY57_tree=null;
		CommonTree METHOD_HANDLE_TYPE_FIELD58_tree=null;
		CommonTree METHOD_HANDLE_TYPE_METHOD59_tree=null;
		CommonTree INSTRUCTION_FORMAT10t60_tree=null;
		CommonTree INSTRUCTION_FORMAT10x61_tree=null;
		CommonTree INSTRUCTION_FORMAT10x_ODEX62_tree=null;
		CommonTree INSTRUCTION_FORMAT11x63_tree=null;
		CommonTree INSTRUCTION_FORMAT12x_OR_ID64_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_FIELD65_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_FIELD_ODEX66_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_METHOD_HANDLE67_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_METHOD_TYPE68_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_STRING69_tree=null;
		CommonTree INSTRUCTION_FORMAT21c_TYPE70_tree=null;
		CommonTree INSTRUCTION_FORMAT21t71_tree=null;
		CommonTree INSTRUCTION_FORMAT22c_FIELD72_tree=null;
		CommonTree INSTRUCTION_FORMAT22c_FIELD_ODEX73_tree=null;
		CommonTree INSTRUCTION_FORMAT22c_TYPE74_tree=null;
		CommonTree INSTRUCTION_FORMAT22cs_FIELD75_tree=null;
		CommonTree INSTRUCTION_FORMAT22s_OR_ID76_tree=null;
		CommonTree INSTRUCTION_FORMAT22t77_tree=null;
		CommonTree INSTRUCTION_FORMAT23x78_tree=null;
		CommonTree INSTRUCTION_FORMAT31i_OR_ID79_tree=null;
		CommonTree INSTRUCTION_FORMAT31t80_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_CALL_SITE81_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_METHOD82_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_METHOD_ODEX83_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE84_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_TYPE85_tree=null;
		CommonTree INSTRUCTION_FORMAT35mi_METHOD86_tree=null;
		CommonTree INSTRUCTION_FORMAT35ms_METHOD87_tree=null;
		CommonTree INSTRUCTION_FORMAT45cc_METHOD88_tree=null;
		CommonTree INSTRUCTION_FORMAT4rcc_METHOD89_tree=null;
		CommonTree INSTRUCTION_FORMAT51l90_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE");
		RewriteRuleTokenStream stream_ANNOTATION_VISIBILITY=new RewriteRuleTokenStream(adaptor,"token ANNOTATION_VISIBILITY");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22t");
		RewriteRuleTokenStream stream_VOID_TYPE=new RewriteRuleTokenStream(adaptor,"token VOID_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT10t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT10t");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35mi_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35mi_METHOD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22s_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22s_OR_ID");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22cs_FIELD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22cs_FIELD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT12x_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT12x_OR_ID");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35ms_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35ms_METHOD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_METHOD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT45cc_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT45cc_METHOD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT10x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT10x");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_METHOD_HANDLE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_METHOD_HANDLE");
		RewriteRuleTokenStream stream_FLOAT_LITERAL_OR_ID=new RewriteRuleTokenStream(adaptor,"token FLOAT_LITERAL_OR_ID");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_STRING=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_STRING");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_METHOD_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_METHOD_ODEX");
		RewriteRuleTokenStream stream_NEGATIVE_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token NEGATIVE_INTEGER_LITERAL");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_FIELD_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_FIELD_ODEX");
		RewriteRuleTokenStream stream_DOUBLE_LITERAL_OR_ID=new RewriteRuleTokenStream(adaptor,"token DOUBLE_LITERAL_OR_ID");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT31i_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT31i_OR_ID");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21t");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT31t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT31t");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT23x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT23x");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_CALL_SITE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_CALL_SITE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT51l=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT51l");
		RewriteRuleTokenStream stream_POSITIVE_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token POSITIVE_INTEGER_LITERAL");
		RewriteRuleTokenStream stream_BOOL_LITERAL=new RewriteRuleTokenStream(adaptor,"token BOOL_LITERAL");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT10x_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT10x_ODEX");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_FIELD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_FIELD");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_FIELD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_FIELD");
		RewriteRuleTokenStream stream_VERIFICATION_ERROR_TYPE=new RewriteRuleTokenStream(adaptor,"token VERIFICATION_ERROR_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT11x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT11x");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_METHOD_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_METHOD_TYPE");
		RewriteRuleTokenStream stream_ACCESS_SPEC=new RewriteRuleTokenStream(adaptor,"token ACCESS_SPEC");
		RewriteRuleTokenStream stream_NULL_LITERAL=new RewriteRuleTokenStream(adaptor,"token NULL_LITERAL");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT4rcc_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT4rcc_METHOD");
		RewriteRuleTokenStream stream_PRIMITIVE_TYPE=new RewriteRuleTokenStream(adaptor,"token PRIMITIVE_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_FIELD_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_FIELD_ODEX");
		RewriteRuleSubtreeStream stream_param_list_or_id=new RewriteRuleSubtreeStream(adaptor,"rule param_list_or_id");

		try {
			// smaliParser.g:550:3: ( SIMPLE_NAME | ACCESS_SPEC -> SIMPLE_NAME[$ACCESS_SPEC] | VERIFICATION_ERROR_TYPE -> SIMPLE_NAME[$VERIFICATION_ERROR_TYPE] | POSITIVE_INTEGER_LITERAL -> SIMPLE_NAME[$POSITIVE_INTEGER_LITERAL] | NEGATIVE_INTEGER_LITERAL -> SIMPLE_NAME[$NEGATIVE_INTEGER_LITERAL] | FLOAT_LITERAL_OR_ID -> SIMPLE_NAME[$FLOAT_LITERAL_OR_ID] | DOUBLE_LITERAL_OR_ID -> SIMPLE_NAME[$DOUBLE_LITERAL_OR_ID] | BOOL_LITERAL -> SIMPLE_NAME[$BOOL_LITERAL] | NULL_LITERAL -> SIMPLE_NAME[$NULL_LITERAL] | REGISTER -> SIMPLE_NAME[$REGISTER] | param_list_or_id ->| PRIMITIVE_TYPE -> SIMPLE_NAME[$PRIMITIVE_TYPE] | VOID_TYPE -> SIMPLE_NAME[$VOID_TYPE] | ANNOTATION_VISIBILITY -> SIMPLE_NAME[$ANNOTATION_VISIBILITY] | METHOD_HANDLE_TYPE_FIELD | METHOD_HANDLE_TYPE_METHOD | INSTRUCTION_FORMAT10t -> SIMPLE_NAME[$INSTRUCTION_FORMAT10t] | INSTRUCTION_FORMAT10x -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x] | INSTRUCTION_FORMAT10x_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x_ODEX] | INSTRUCTION_FORMAT11x -> SIMPLE_NAME[$INSTRUCTION_FORMAT11x] | INSTRUCTION_FORMAT12x_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT12x_OR_ID] | INSTRUCTION_FORMAT21c_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD] | INSTRUCTION_FORMAT21c_FIELD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD_ODEX] | INSTRUCTION_FORMAT21c_METHOD_HANDLE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_HANDLE] | INSTRUCTION_FORMAT21c_METHOD_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_TYPE] | INSTRUCTION_FORMAT21c_STRING -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_STRING] | INSTRUCTION_FORMAT21c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_TYPE] | INSTRUCTION_FORMAT21t -> SIMPLE_NAME[$INSTRUCTION_FORMAT21t] | INSTRUCTION_FORMAT22c_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD] | INSTRUCTION_FORMAT22c_FIELD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD_ODEX] | INSTRUCTION_FORMAT22c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_TYPE] | INSTRUCTION_FORMAT22cs_FIELD -> SIMPLE_NAME[$INSTRUCTION_FORMAT22cs_FIELD] | INSTRUCTION_FORMAT22s_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT22s_OR_ID] | INSTRUCTION_FORMAT22t -> SIMPLE_NAME[$INSTRUCTION_FORMAT22t] | INSTRUCTION_FORMAT23x -> SIMPLE_NAME[$INSTRUCTION_FORMAT23x] | INSTRUCTION_FORMAT31i_OR_ID -> SIMPLE_NAME[$INSTRUCTION_FORMAT31i_OR_ID] | INSTRUCTION_FORMAT31t -> SIMPLE_NAME[$INSTRUCTION_FORMAT31t] | INSTRUCTION_FORMAT35c_CALL_SITE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_CALL_SITE] | INSTRUCTION_FORMAT35c_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD] | INSTRUCTION_FORMAT35c_METHOD_ODEX -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_ODEX] | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE] | INSTRUCTION_FORMAT35c_TYPE -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_TYPE] | INSTRUCTION_FORMAT35mi_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35mi_METHOD] | INSTRUCTION_FORMAT35ms_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT35ms_METHOD] | INSTRUCTION_FORMAT45cc_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT45cc_METHOD] | INSTRUCTION_FORMAT4rcc_METHOD -> SIMPLE_NAME[$INSTRUCTION_FORMAT4rcc_METHOD] | INSTRUCTION_FORMAT51l -> SIMPLE_NAME[$INSTRUCTION_FORMAT51l] )
			int alt10=47;
			switch ( input.LA(1) ) {
			case SIMPLE_NAME:
				{
				alt10=1;
				}
				break;
			case ACCESS_SPEC:
				{
				alt10=2;
				}
				break;
			case VERIFICATION_ERROR_TYPE:
				{
				alt10=3;
				}
				break;
			case POSITIVE_INTEGER_LITERAL:
				{
				alt10=4;
				}
				break;
			case NEGATIVE_INTEGER_LITERAL:
				{
				alt10=5;
				}
				break;
			case FLOAT_LITERAL_OR_ID:
				{
				alt10=6;
				}
				break;
			case DOUBLE_LITERAL_OR_ID:
				{
				alt10=7;
				}
				break;
			case BOOL_LITERAL:
				{
				alt10=8;
				}
				break;
			case NULL_LITERAL:
				{
				alt10=9;
				}
				break;
			case REGISTER:
				{
				alt10=10;
				}
				break;
			case PARAM_LIST_OR_ID_PRIMITIVE_TYPE:
				{
				alt10=11;
				}
				break;
			case PRIMITIVE_TYPE:
				{
				alt10=12;
				}
				break;
			case VOID_TYPE:
				{
				alt10=13;
				}
				break;
			case ANNOTATION_VISIBILITY:
				{
				alt10=14;
				}
				break;
			case METHOD_HANDLE_TYPE_FIELD:
				{
				alt10=15;
				}
				break;
			case METHOD_HANDLE_TYPE_METHOD:
				{
				alt10=16;
				}
				break;
			case INSTRUCTION_FORMAT10t:
				{
				alt10=17;
				}
				break;
			case INSTRUCTION_FORMAT10x:
				{
				alt10=18;
				}
				break;
			case INSTRUCTION_FORMAT10x_ODEX:
				{
				alt10=19;
				}
				break;
			case INSTRUCTION_FORMAT11x:
				{
				alt10=20;
				}
				break;
			case INSTRUCTION_FORMAT12x_OR_ID:
				{
				alt10=21;
				}
				break;
			case INSTRUCTION_FORMAT21c_FIELD:
				{
				alt10=22;
				}
				break;
			case INSTRUCTION_FORMAT21c_FIELD_ODEX:
				{
				alt10=23;
				}
				break;
			case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
				{
				alt10=24;
				}
				break;
			case INSTRUCTION_FORMAT21c_METHOD_TYPE:
				{
				alt10=25;
				}
				break;
			case INSTRUCTION_FORMAT21c_STRING:
				{
				alt10=26;
				}
				break;
			case INSTRUCTION_FORMAT21c_TYPE:
				{
				alt10=27;
				}
				break;
			case INSTRUCTION_FORMAT21t:
				{
				alt10=28;
				}
				break;
			case INSTRUCTION_FORMAT22c_FIELD:
				{
				alt10=29;
				}
				break;
			case INSTRUCTION_FORMAT22c_FIELD_ODEX:
				{
				alt10=30;
				}
				break;
			case INSTRUCTION_FORMAT22c_TYPE:
				{
				alt10=31;
				}
				break;
			case INSTRUCTION_FORMAT22cs_FIELD:
				{
				alt10=32;
				}
				break;
			case INSTRUCTION_FORMAT22s_OR_ID:
				{
				alt10=33;
				}
				break;
			case INSTRUCTION_FORMAT22t:
				{
				alt10=34;
				}
				break;
			case INSTRUCTION_FORMAT23x:
				{
				alt10=35;
				}
				break;
			case INSTRUCTION_FORMAT31i_OR_ID:
				{
				alt10=36;
				}
				break;
			case INSTRUCTION_FORMAT31t:
				{
				alt10=37;
				}
				break;
			case INSTRUCTION_FORMAT35c_CALL_SITE:
				{
				alt10=38;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD:
				{
				alt10=39;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD_ODEX:
				{
				alt10=40;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
				{
				alt10=41;
				}
				break;
			case INSTRUCTION_FORMAT35c_TYPE:
				{
				alt10=42;
				}
				break;
			case INSTRUCTION_FORMAT35mi_METHOD:
				{
				alt10=43;
				}
				break;
			case INSTRUCTION_FORMAT35ms_METHOD:
				{
				alt10=44;
				}
				break;
			case INSTRUCTION_FORMAT45cc_METHOD:
				{
				alt10=45;
				}
				break;
			case INSTRUCTION_FORMAT4rcc_METHOD:
				{
				alt10=46;
				}
				break;
			case INSTRUCTION_FORMAT51l:
				{
				alt10=47;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 10, 0, input);
				throw nvae;
			}
			switch (alt10) {
				case 1 :
					// smaliParser.g:550:5: SIMPLE_NAME
					{
					root_0 = (CommonTree)adaptor.nil();


					SIMPLE_NAME44=(Token)match(input,SIMPLE_NAME,FOLLOW_SIMPLE_NAME_in_simple_name1842);
					SIMPLE_NAME44_tree = (CommonTree)adaptor.create(SIMPLE_NAME44);
					adaptor.addChild(root_0, SIMPLE_NAME44_tree);

					}
					break;
				case 2 :
					// smaliParser.g:551:5: ACCESS_SPEC
					{
					ACCESS_SPEC45=(Token)match(input,ACCESS_SPEC,FOLLOW_ACCESS_SPEC_in_simple_name1848);
					stream_ACCESS_SPEC.add(ACCESS_SPEC45);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 551:17: -> SIMPLE_NAME[$ACCESS_SPEC]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, ACCESS_SPEC45));
					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// smaliParser.g:552:5: VERIFICATION_ERROR_TYPE
					{
					VERIFICATION_ERROR_TYPE46=(Token)match(input,VERIFICATION_ERROR_TYPE,FOLLOW_VERIFICATION_ERROR_TYPE_in_simple_name1859);
					stream_VERIFICATION_ERROR_TYPE.add(VERIFICATION_ERROR_TYPE46);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 552:29: -> SIMPLE_NAME[$VERIFICATION_ERROR_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, VERIFICATION_ERROR_TYPE46));
					}


					retval.tree = root_0;

					}
					break;
				case 4 :
					// smaliParser.g:553:5: POSITIVE_INTEGER_LITERAL
					{
					POSITIVE_INTEGER_LITERAL47=(Token)match(input,POSITIVE_INTEGER_LITERAL,FOLLOW_POSITIVE_INTEGER_LITERAL_in_simple_name1870);
					stream_POSITIVE_INTEGER_LITERAL.add(POSITIVE_INTEGER_LITERAL47);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 553:30: -> SIMPLE_NAME[$POSITIVE_INTEGER_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, POSITIVE_INTEGER_LITERAL47));
					}


					retval.tree = root_0;

					}
					break;
				case 5 :
					// smaliParser.g:554:5: NEGATIVE_INTEGER_LITERAL
					{
					NEGATIVE_INTEGER_LITERAL48=(Token)match(input,NEGATIVE_INTEGER_LITERAL,FOLLOW_NEGATIVE_INTEGER_LITERAL_in_simple_name1881);
					stream_NEGATIVE_INTEGER_LITERAL.add(NEGATIVE_INTEGER_LITERAL48);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 554:30: -> SIMPLE_NAME[$NEGATIVE_INTEGER_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, NEGATIVE_INTEGER_LITERAL48));
					}


					retval.tree = root_0;

					}
					break;
				case 6 :
					// smaliParser.g:555:5: FLOAT_LITERAL_OR_ID
					{
					FLOAT_LITERAL_OR_ID49=(Token)match(input,FLOAT_LITERAL_OR_ID,FOLLOW_FLOAT_LITERAL_OR_ID_in_simple_name1892);
					stream_FLOAT_LITERAL_OR_ID.add(FLOAT_LITERAL_OR_ID49);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 555:25: -> SIMPLE_NAME[$FLOAT_LITERAL_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, FLOAT_LITERAL_OR_ID49));
					}


					retval.tree = root_0;

					}
					break;
				case 7 :
					// smaliParser.g:556:5: DOUBLE_LITERAL_OR_ID
					{
					DOUBLE_LITERAL_OR_ID50=(Token)match(input,DOUBLE_LITERAL_OR_ID,FOLLOW_DOUBLE_LITERAL_OR_ID_in_simple_name1903);
					stream_DOUBLE_LITERAL_OR_ID.add(DOUBLE_LITERAL_OR_ID50);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 556:26: -> SIMPLE_NAME[$DOUBLE_LITERAL_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, DOUBLE_LITERAL_OR_ID50));
					}


					retval.tree = root_0;

					}
					break;
				case 8 :
					// smaliParser.g:557:5: BOOL_LITERAL
					{
					BOOL_LITERAL51=(Token)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_simple_name1914);
					stream_BOOL_LITERAL.add(BOOL_LITERAL51);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 557:18: -> SIMPLE_NAME[$BOOL_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, BOOL_LITERAL51));
					}


					retval.tree = root_0;

					}
					break;
				case 9 :
					// smaliParser.g:558:5: NULL_LITERAL
					{
					NULL_LITERAL52=(Token)match(input,NULL_LITERAL,FOLLOW_NULL_LITERAL_in_simple_name1925);
					stream_NULL_LITERAL.add(NULL_LITERAL52);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 558:18: -> SIMPLE_NAME[$NULL_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, NULL_LITERAL52));
					}


					retval.tree = root_0;

					}
					break;
				case 10 :
					// smaliParser.g:559:5: REGISTER
					{
					REGISTER53=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_simple_name1936);
					stream_REGISTER.add(REGISTER53);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 559:14: -> SIMPLE_NAME[$REGISTER]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, REGISTER53));
					}


					retval.tree = root_0;

					}
					break;
				case 11 :
					// smaliParser.g:560:5: param_list_or_id
					{
					pushFollow(FOLLOW_param_list_or_id_in_simple_name1947);
					param_list_or_id54=param_list_or_id();
					state._fsp--;

					stream_param_list_or_id.add(param_list_or_id54.getTree());
					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 560:22: ->
					{
						adaptor.addChild(root_0,  adaptor.create(SIMPLE_NAME, (param_list_or_id54!=null?input.toString(param_list_or_id54.start,param_list_or_id54.stop):null)) );
					}


					retval.tree = root_0;

					}
					break;
				case 12 :
					// smaliParser.g:561:5: PRIMITIVE_TYPE
					{
					PRIMITIVE_TYPE55=(Token)match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_simple_name1957);
					stream_PRIMITIVE_TYPE.add(PRIMITIVE_TYPE55);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 561:20: -> SIMPLE_NAME[$PRIMITIVE_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, PRIMITIVE_TYPE55));
					}


					retval.tree = root_0;

					}
					break;
				case 13 :
					// smaliParser.g:562:5: VOID_TYPE
					{
					VOID_TYPE56=(Token)match(input,VOID_TYPE,FOLLOW_VOID_TYPE_in_simple_name1968);
					stream_VOID_TYPE.add(VOID_TYPE56);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 562:15: -> SIMPLE_NAME[$VOID_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, VOID_TYPE56));
					}


					retval.tree = root_0;

					}
					break;
				case 14 :
					// smaliParser.g:563:5: ANNOTATION_VISIBILITY
					{
					ANNOTATION_VISIBILITY57=(Token)match(input,ANNOTATION_VISIBILITY,FOLLOW_ANNOTATION_VISIBILITY_in_simple_name1979);
					stream_ANNOTATION_VISIBILITY.add(ANNOTATION_VISIBILITY57);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 563:27: -> SIMPLE_NAME[$ANNOTATION_VISIBILITY]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, ANNOTATION_VISIBILITY57));
					}


					retval.tree = root_0;

					}
					break;
				case 15 :
					// smaliParser.g:564:5: METHOD_HANDLE_TYPE_FIELD
					{
					root_0 = (CommonTree)adaptor.nil();


					METHOD_HANDLE_TYPE_FIELD58=(Token)match(input,METHOD_HANDLE_TYPE_FIELD,FOLLOW_METHOD_HANDLE_TYPE_FIELD_in_simple_name1990);
					METHOD_HANDLE_TYPE_FIELD58_tree = (CommonTree)adaptor.create(METHOD_HANDLE_TYPE_FIELD58);
					adaptor.addChild(root_0, METHOD_HANDLE_TYPE_FIELD58_tree);

					}
					break;
				case 16 :
					// smaliParser.g:565:5: METHOD_HANDLE_TYPE_METHOD
					{
					root_0 = (CommonTree)adaptor.nil();


					METHOD_HANDLE_TYPE_METHOD59=(Token)match(input,METHOD_HANDLE_TYPE_METHOD,FOLLOW_METHOD_HANDLE_TYPE_METHOD_in_simple_name1996);
					METHOD_HANDLE_TYPE_METHOD59_tree = (CommonTree)adaptor.create(METHOD_HANDLE_TYPE_METHOD59);
					adaptor.addChild(root_0, METHOD_HANDLE_TYPE_METHOD59_tree);

					}
					break;
				case 17 :
					// smaliParser.g:566:5: INSTRUCTION_FORMAT10t
					{
					INSTRUCTION_FORMAT10t60=(Token)match(input,INSTRUCTION_FORMAT10t,FOLLOW_INSTRUCTION_FORMAT10t_in_simple_name2002);
					stream_INSTRUCTION_FORMAT10t.add(INSTRUCTION_FORMAT10t60);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 566:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT10t]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT10t60));
					}


					retval.tree = root_0;

					}
					break;
				case 18 :
					// smaliParser.g:567:5: INSTRUCTION_FORMAT10x
					{
					INSTRUCTION_FORMAT10x61=(Token)match(input,INSTRUCTION_FORMAT10x,FOLLOW_INSTRUCTION_FORMAT10x_in_simple_name2013);
					stream_INSTRUCTION_FORMAT10x.add(INSTRUCTION_FORMAT10x61);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 567:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT10x61));
					}


					retval.tree = root_0;

					}
					break;
				case 19 :
					// smaliParser.g:568:5: INSTRUCTION_FORMAT10x_ODEX
					{
					INSTRUCTION_FORMAT10x_ODEX62=(Token)match(input,INSTRUCTION_FORMAT10x_ODEX,FOLLOW_INSTRUCTION_FORMAT10x_ODEX_in_simple_name2024);
					stream_INSTRUCTION_FORMAT10x_ODEX.add(INSTRUCTION_FORMAT10x_ODEX62);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 568:32: -> SIMPLE_NAME[$INSTRUCTION_FORMAT10x_ODEX]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT10x_ODEX62));
					}


					retval.tree = root_0;

					}
					break;
				case 20 :
					// smaliParser.g:569:5: INSTRUCTION_FORMAT11x
					{
					INSTRUCTION_FORMAT11x63=(Token)match(input,INSTRUCTION_FORMAT11x,FOLLOW_INSTRUCTION_FORMAT11x_in_simple_name2035);
					stream_INSTRUCTION_FORMAT11x.add(INSTRUCTION_FORMAT11x63);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 569:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT11x]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT11x63));
					}


					retval.tree = root_0;

					}
					break;
				case 21 :
					// smaliParser.g:570:5: INSTRUCTION_FORMAT12x_OR_ID
					{
					INSTRUCTION_FORMAT12x_OR_ID64=(Token)match(input,INSTRUCTION_FORMAT12x_OR_ID,FOLLOW_INSTRUCTION_FORMAT12x_OR_ID_in_simple_name2046);
					stream_INSTRUCTION_FORMAT12x_OR_ID.add(INSTRUCTION_FORMAT12x_OR_ID64);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 570:33: -> SIMPLE_NAME[$INSTRUCTION_FORMAT12x_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT12x_OR_ID64));
					}


					retval.tree = root_0;

					}
					break;
				case 22 :
					// smaliParser.g:571:5: INSTRUCTION_FORMAT21c_FIELD
					{
					INSTRUCTION_FORMAT21c_FIELD65=(Token)match(input,INSTRUCTION_FORMAT21c_FIELD,FOLLOW_INSTRUCTION_FORMAT21c_FIELD_in_simple_name2057);
					stream_INSTRUCTION_FORMAT21c_FIELD.add(INSTRUCTION_FORMAT21c_FIELD65);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 571:33: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_FIELD65));
					}


					retval.tree = root_0;

					}
					break;
				case 23 :
					// smaliParser.g:572:5: INSTRUCTION_FORMAT21c_FIELD_ODEX
					{
					INSTRUCTION_FORMAT21c_FIELD_ODEX66=(Token)match(input,INSTRUCTION_FORMAT21c_FIELD_ODEX,FOLLOW_INSTRUCTION_FORMAT21c_FIELD_ODEX_in_simple_name2068);
					stream_INSTRUCTION_FORMAT21c_FIELD_ODEX.add(INSTRUCTION_FORMAT21c_FIELD_ODEX66);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 572:38: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_FIELD_ODEX]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_FIELD_ODEX66));
					}


					retval.tree = root_0;

					}
					break;
				case 24 :
					// smaliParser.g:573:5: INSTRUCTION_FORMAT21c_METHOD_HANDLE
					{
					INSTRUCTION_FORMAT21c_METHOD_HANDLE67=(Token)match(input,INSTRUCTION_FORMAT21c_METHOD_HANDLE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_simple_name2079);
					stream_INSTRUCTION_FORMAT21c_METHOD_HANDLE.add(INSTRUCTION_FORMAT21c_METHOD_HANDLE67);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 573:41: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_HANDLE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_METHOD_HANDLE67));
					}


					retval.tree = root_0;

					}
					break;
				case 25 :
					// smaliParser.g:574:5: INSTRUCTION_FORMAT21c_METHOD_TYPE
					{
					INSTRUCTION_FORMAT21c_METHOD_TYPE68=(Token)match(input,INSTRUCTION_FORMAT21c_METHOD_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_simple_name2090);
					stream_INSTRUCTION_FORMAT21c_METHOD_TYPE.add(INSTRUCTION_FORMAT21c_METHOD_TYPE68);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 574:39: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_METHOD_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_METHOD_TYPE68));
					}


					retval.tree = root_0;

					}
					break;
				case 26 :
					// smaliParser.g:575:5: INSTRUCTION_FORMAT21c_STRING
					{
					INSTRUCTION_FORMAT21c_STRING69=(Token)match(input,INSTRUCTION_FORMAT21c_STRING,FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_simple_name2101);
					stream_INSTRUCTION_FORMAT21c_STRING.add(INSTRUCTION_FORMAT21c_STRING69);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 575:34: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_STRING]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_STRING69));
					}


					retval.tree = root_0;

					}
					break;
				case 27 :
					// smaliParser.g:576:5: INSTRUCTION_FORMAT21c_TYPE
					{
					INSTRUCTION_FORMAT21c_TYPE70=(Token)match(input,INSTRUCTION_FORMAT21c_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_simple_name2112);
					stream_INSTRUCTION_FORMAT21c_TYPE.add(INSTRUCTION_FORMAT21c_TYPE70);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 576:32: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21c_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21c_TYPE70));
					}


					retval.tree = root_0;

					}
					break;
				case 28 :
					// smaliParser.g:577:5: INSTRUCTION_FORMAT21t
					{
					INSTRUCTION_FORMAT21t71=(Token)match(input,INSTRUCTION_FORMAT21t,FOLLOW_INSTRUCTION_FORMAT21t_in_simple_name2123);
					stream_INSTRUCTION_FORMAT21t.add(INSTRUCTION_FORMAT21t71);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 577:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT21t]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT21t71));
					}


					retval.tree = root_0;

					}
					break;
				case 29 :
					// smaliParser.g:578:5: INSTRUCTION_FORMAT22c_FIELD
					{
					INSTRUCTION_FORMAT22c_FIELD72=(Token)match(input,INSTRUCTION_FORMAT22c_FIELD,FOLLOW_INSTRUCTION_FORMAT22c_FIELD_in_simple_name2134);
					stream_INSTRUCTION_FORMAT22c_FIELD.add(INSTRUCTION_FORMAT22c_FIELD72);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 578:33: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22c_FIELD72));
					}


					retval.tree = root_0;

					}
					break;
				case 30 :
					// smaliParser.g:579:5: INSTRUCTION_FORMAT22c_FIELD_ODEX
					{
					INSTRUCTION_FORMAT22c_FIELD_ODEX73=(Token)match(input,INSTRUCTION_FORMAT22c_FIELD_ODEX,FOLLOW_INSTRUCTION_FORMAT22c_FIELD_ODEX_in_simple_name2145);
					stream_INSTRUCTION_FORMAT22c_FIELD_ODEX.add(INSTRUCTION_FORMAT22c_FIELD_ODEX73);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 579:38: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_FIELD_ODEX]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22c_FIELD_ODEX73));
					}


					retval.tree = root_0;

					}
					break;
				case 31 :
					// smaliParser.g:580:5: INSTRUCTION_FORMAT22c_TYPE
					{
					INSTRUCTION_FORMAT22c_TYPE74=(Token)match(input,INSTRUCTION_FORMAT22c_TYPE,FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_simple_name2156);
					stream_INSTRUCTION_FORMAT22c_TYPE.add(INSTRUCTION_FORMAT22c_TYPE74);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 580:32: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22c_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22c_TYPE74));
					}


					retval.tree = root_0;

					}
					break;
				case 32 :
					// smaliParser.g:581:5: INSTRUCTION_FORMAT22cs_FIELD
					{
					INSTRUCTION_FORMAT22cs_FIELD75=(Token)match(input,INSTRUCTION_FORMAT22cs_FIELD,FOLLOW_INSTRUCTION_FORMAT22cs_FIELD_in_simple_name2167);
					stream_INSTRUCTION_FORMAT22cs_FIELD.add(INSTRUCTION_FORMAT22cs_FIELD75);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 581:34: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22cs_FIELD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22cs_FIELD75));
					}


					retval.tree = root_0;

					}
					break;
				case 33 :
					// smaliParser.g:582:5: INSTRUCTION_FORMAT22s_OR_ID
					{
					INSTRUCTION_FORMAT22s_OR_ID76=(Token)match(input,INSTRUCTION_FORMAT22s_OR_ID,FOLLOW_INSTRUCTION_FORMAT22s_OR_ID_in_simple_name2178);
					stream_INSTRUCTION_FORMAT22s_OR_ID.add(INSTRUCTION_FORMAT22s_OR_ID76);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 582:33: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22s_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22s_OR_ID76));
					}


					retval.tree = root_0;

					}
					break;
				case 34 :
					// smaliParser.g:583:5: INSTRUCTION_FORMAT22t
					{
					INSTRUCTION_FORMAT22t77=(Token)match(input,INSTRUCTION_FORMAT22t,FOLLOW_INSTRUCTION_FORMAT22t_in_simple_name2189);
					stream_INSTRUCTION_FORMAT22t.add(INSTRUCTION_FORMAT22t77);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 583:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT22t]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT22t77));
					}


					retval.tree = root_0;

					}
					break;
				case 35 :
					// smaliParser.g:584:5: INSTRUCTION_FORMAT23x
					{
					INSTRUCTION_FORMAT23x78=(Token)match(input,INSTRUCTION_FORMAT23x,FOLLOW_INSTRUCTION_FORMAT23x_in_simple_name2200);
					stream_INSTRUCTION_FORMAT23x.add(INSTRUCTION_FORMAT23x78);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 584:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT23x]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT23x78));
					}


					retval.tree = root_0;

					}
					break;
				case 36 :
					// smaliParser.g:585:5: INSTRUCTION_FORMAT31i_OR_ID
					{
					INSTRUCTION_FORMAT31i_OR_ID79=(Token)match(input,INSTRUCTION_FORMAT31i_OR_ID,FOLLOW_INSTRUCTION_FORMAT31i_OR_ID_in_simple_name2211);
					stream_INSTRUCTION_FORMAT31i_OR_ID.add(INSTRUCTION_FORMAT31i_OR_ID79);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 585:33: -> SIMPLE_NAME[$INSTRUCTION_FORMAT31i_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT31i_OR_ID79));
					}


					retval.tree = root_0;

					}
					break;
				case 37 :
					// smaliParser.g:586:5: INSTRUCTION_FORMAT31t
					{
					INSTRUCTION_FORMAT31t80=(Token)match(input,INSTRUCTION_FORMAT31t,FOLLOW_INSTRUCTION_FORMAT31t_in_simple_name2222);
					stream_INSTRUCTION_FORMAT31t.add(INSTRUCTION_FORMAT31t80);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 586:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT31t]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT31t80));
					}


					retval.tree = root_0;

					}
					break;
				case 38 :
					// smaliParser.g:587:5: INSTRUCTION_FORMAT35c_CALL_SITE
					{
					INSTRUCTION_FORMAT35c_CALL_SITE81=(Token)match(input,INSTRUCTION_FORMAT35c_CALL_SITE,FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_simple_name2233);
					stream_INSTRUCTION_FORMAT35c_CALL_SITE.add(INSTRUCTION_FORMAT35c_CALL_SITE81);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 587:37: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_CALL_SITE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35c_CALL_SITE81));
					}


					retval.tree = root_0;

					}
					break;
				case 39 :
					// smaliParser.g:588:5: INSTRUCTION_FORMAT35c_METHOD
					{
					INSTRUCTION_FORMAT35c_METHOD82=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_simple_name2244);
					stream_INSTRUCTION_FORMAT35c_METHOD.add(INSTRUCTION_FORMAT35c_METHOD82);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 588:34: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35c_METHOD82));
					}


					retval.tree = root_0;

					}
					break;
				case 40 :
					// smaliParser.g:589:5: INSTRUCTION_FORMAT35c_METHOD_ODEX
					{
					INSTRUCTION_FORMAT35c_METHOD_ODEX83=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD_ODEX,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_ODEX_in_simple_name2255);
					stream_INSTRUCTION_FORMAT35c_METHOD_ODEX.add(INSTRUCTION_FORMAT35c_METHOD_ODEX83);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 589:39: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_ODEX]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35c_METHOD_ODEX83));
					}


					retval.tree = root_0;

					}
					break;
				case 41 :
					// smaliParser.g:590:5: INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE
					{
					INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE84=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_simple_name2266);
					stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE.add(INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE84);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 590:56: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE84));
					}


					retval.tree = root_0;

					}
					break;
				case 42 :
					// smaliParser.g:591:5: INSTRUCTION_FORMAT35c_TYPE
					{
					INSTRUCTION_FORMAT35c_TYPE85=(Token)match(input,INSTRUCTION_FORMAT35c_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_simple_name2277);
					stream_INSTRUCTION_FORMAT35c_TYPE.add(INSTRUCTION_FORMAT35c_TYPE85);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 591:32: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35c_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35c_TYPE85));
					}


					retval.tree = root_0;

					}
					break;
				case 43 :
					// smaliParser.g:592:5: INSTRUCTION_FORMAT35mi_METHOD
					{
					INSTRUCTION_FORMAT35mi_METHOD86=(Token)match(input,INSTRUCTION_FORMAT35mi_METHOD,FOLLOW_INSTRUCTION_FORMAT35mi_METHOD_in_simple_name2288);
					stream_INSTRUCTION_FORMAT35mi_METHOD.add(INSTRUCTION_FORMAT35mi_METHOD86);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 592:35: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35mi_METHOD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35mi_METHOD86));
					}


					retval.tree = root_0;

					}
					break;
				case 44 :
					// smaliParser.g:593:5: INSTRUCTION_FORMAT35ms_METHOD
					{
					INSTRUCTION_FORMAT35ms_METHOD87=(Token)match(input,INSTRUCTION_FORMAT35ms_METHOD,FOLLOW_INSTRUCTION_FORMAT35ms_METHOD_in_simple_name2299);
					stream_INSTRUCTION_FORMAT35ms_METHOD.add(INSTRUCTION_FORMAT35ms_METHOD87);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 593:35: -> SIMPLE_NAME[$INSTRUCTION_FORMAT35ms_METHOD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT35ms_METHOD87));
					}


					retval.tree = root_0;

					}
					break;
				case 45 :
					// smaliParser.g:594:5: INSTRUCTION_FORMAT45cc_METHOD
					{
					INSTRUCTION_FORMAT45cc_METHOD88=(Token)match(input,INSTRUCTION_FORMAT45cc_METHOD,FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_simple_name2310);
					stream_INSTRUCTION_FORMAT45cc_METHOD.add(INSTRUCTION_FORMAT45cc_METHOD88);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 594:35: -> SIMPLE_NAME[$INSTRUCTION_FORMAT45cc_METHOD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT45cc_METHOD88));
					}


					retval.tree = root_0;

					}
					break;
				case 46 :
					// smaliParser.g:595:5: INSTRUCTION_FORMAT4rcc_METHOD
					{
					INSTRUCTION_FORMAT4rcc_METHOD89=(Token)match(input,INSTRUCTION_FORMAT4rcc_METHOD,FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_simple_name2321);
					stream_INSTRUCTION_FORMAT4rcc_METHOD.add(INSTRUCTION_FORMAT4rcc_METHOD89);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 595:35: -> SIMPLE_NAME[$INSTRUCTION_FORMAT4rcc_METHOD]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT4rcc_METHOD89));
					}


					retval.tree = root_0;

					}
					break;
				case 47 :
					// smaliParser.g:596:5: INSTRUCTION_FORMAT51l
					{
					INSTRUCTION_FORMAT51l90=(Token)match(input,INSTRUCTION_FORMAT51l,FOLLOW_INSTRUCTION_FORMAT51l_in_simple_name2332);
					stream_INSTRUCTION_FORMAT51l.add(INSTRUCTION_FORMAT51l90);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 596:27: -> SIMPLE_NAME[$INSTRUCTION_FORMAT51l]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, INSTRUCTION_FORMAT51l90));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simple_name"


	public static class member_name_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "member_name"
	// smaliParser.g:598:1: member_name : ( simple_name | MEMBER_NAME -> SIMPLE_NAME[$MEMBER_NAME] );
	public final smaliParser.member_name_return member_name() throws RecognitionException {
		smaliParser.member_name_return retval = new smaliParser.member_name_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token MEMBER_NAME92=null;
		ParserRuleReturnScope simple_name91 =null;

		CommonTree MEMBER_NAME92_tree=null;
		RewriteRuleTokenStream stream_MEMBER_NAME=new RewriteRuleTokenStream(adaptor,"token MEMBER_NAME");

		try {
			// smaliParser.g:599:3: ( simple_name | MEMBER_NAME -> SIMPLE_NAME[$MEMBER_NAME] )
			int alt11=2;
			int LA11_0 = input.LA(1);
			if ( (LA11_0==ACCESS_SPEC||LA11_0==ANNOTATION_VISIBILITY||LA11_0==BOOL_LITERAL||LA11_0==DOUBLE_LITERAL_OR_ID||LA11_0==FLOAT_LITERAL_OR_ID||(LA11_0 >= INSTRUCTION_FORMAT10t && LA11_0 <= INSTRUCTION_FORMAT10x_ODEX)||LA11_0==INSTRUCTION_FORMAT11x||LA11_0==INSTRUCTION_FORMAT12x_OR_ID||(LA11_0 >= INSTRUCTION_FORMAT21c_FIELD && LA11_0 <= INSTRUCTION_FORMAT21c_TYPE)||LA11_0==INSTRUCTION_FORMAT21t||(LA11_0 >= INSTRUCTION_FORMAT22c_FIELD && LA11_0 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA11_0 >= INSTRUCTION_FORMAT22s_OR_ID && LA11_0 <= INSTRUCTION_FORMAT22t)||LA11_0==INSTRUCTION_FORMAT23x||(LA11_0 >= INSTRUCTION_FORMAT31i_OR_ID && LA11_0 <= INSTRUCTION_FORMAT31t)||(LA11_0 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA11_0 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA11_0 >= INSTRUCTION_FORMAT45cc_METHOD && LA11_0 <= INSTRUCTION_FORMAT51l)||(LA11_0 >= METHOD_HANDLE_TYPE_FIELD && LA11_0 <= NULL_LITERAL)||(LA11_0 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA11_0 <= PRIMITIVE_TYPE)||LA11_0==REGISTER||LA11_0==SIMPLE_NAME||(LA11_0 >= VERIFICATION_ERROR_TYPE && LA11_0 <= VOID_TYPE)) ) {
				alt11=1;
			}
			else if ( (LA11_0==MEMBER_NAME) ) {
				alt11=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 11, 0, input);
				throw nvae;
			}

			switch (alt11) {
				case 1 :
					// smaliParser.g:599:5: simple_name
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_simple_name_in_member_name2347);
					simple_name91=simple_name();
					state._fsp--;

					adaptor.addChild(root_0, simple_name91.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:600:5: MEMBER_NAME
					{
					MEMBER_NAME92=(Token)match(input,MEMBER_NAME,FOLLOW_MEMBER_NAME_in_member_name2353);
					stream_MEMBER_NAME.add(MEMBER_NAME92);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 600:17: -> SIMPLE_NAME[$MEMBER_NAME]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(SIMPLE_NAME, MEMBER_NAME92));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "member_name"


	public static class method_prototype_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "method_prototype"
	// smaliParser.g:602:1: method_prototype : OPEN_PAREN param_list CLOSE_PAREN type_descriptor -> ^( I_METHOD_PROTOTYPE[$start, \"I_METHOD_PROTOTYPE\"] ^( I_METHOD_RETURN_TYPE type_descriptor ) ( param_list )? ) ;
	public final smaliParser.method_prototype_return method_prototype() throws RecognitionException {
		smaliParser.method_prototype_return retval = new smaliParser.method_prototype_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPEN_PAREN93=null;
		Token CLOSE_PAREN95=null;
		ParserRuleReturnScope param_list94 =null;
		ParserRuleReturnScope type_descriptor96 =null;

		CommonTree OPEN_PAREN93_tree=null;
		CommonTree CLOSE_PAREN95_tree=null;
		RewriteRuleTokenStream stream_OPEN_PAREN=new RewriteRuleTokenStream(adaptor,"token OPEN_PAREN");
		RewriteRuleTokenStream stream_CLOSE_PAREN=new RewriteRuleTokenStream(adaptor,"token CLOSE_PAREN");
		RewriteRuleSubtreeStream stream_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule type_descriptor");
		RewriteRuleSubtreeStream stream_param_list=new RewriteRuleSubtreeStream(adaptor,"rule param_list");

		try {
			// smaliParser.g:603:3: ( OPEN_PAREN param_list CLOSE_PAREN type_descriptor -> ^( I_METHOD_PROTOTYPE[$start, \"I_METHOD_PROTOTYPE\"] ^( I_METHOD_RETURN_TYPE type_descriptor ) ( param_list )? ) )
			// smaliParser.g:603:5: OPEN_PAREN param_list CLOSE_PAREN type_descriptor
			{
			OPEN_PAREN93=(Token)match(input,OPEN_PAREN,FOLLOW_OPEN_PAREN_in_method_prototype2368);
			stream_OPEN_PAREN.add(OPEN_PAREN93);

			pushFollow(FOLLOW_param_list_in_method_prototype2370);
			param_list94=param_list();
			state._fsp--;

			stream_param_list.add(param_list94.getTree());
			CLOSE_PAREN95=(Token)match(input,CLOSE_PAREN,FOLLOW_CLOSE_PAREN_in_method_prototype2372);
			stream_CLOSE_PAREN.add(CLOSE_PAREN95);

			pushFollow(FOLLOW_type_descriptor_in_method_prototype2374);
			type_descriptor96=type_descriptor();
			state._fsp--;

			stream_type_descriptor.add(type_descriptor96.getTree());
			// AST REWRITE
			// elements: type_descriptor, param_list
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 604:5: -> ^( I_METHOD_PROTOTYPE[$start, \"I_METHOD_PROTOTYPE\"] ^( I_METHOD_RETURN_TYPE type_descriptor ) ( param_list )? )
			{
				// smaliParser.g:604:8: ^( I_METHOD_PROTOTYPE[$start, \"I_METHOD_PROTOTYPE\"] ^( I_METHOD_RETURN_TYPE type_descriptor ) ( param_list )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_METHOD_PROTOTYPE, (retval.start), "I_METHOD_PROTOTYPE"), root_1);
				// smaliParser.g:604:59: ^( I_METHOD_RETURN_TYPE type_descriptor )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_METHOD_RETURN_TYPE, "I_METHOD_RETURN_TYPE"), root_2);
				adaptor.addChild(root_2, stream_type_descriptor.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				// smaliParser.g:604:99: ( param_list )?
				if ( stream_param_list.hasNext() ) {
					adaptor.addChild(root_1, stream_param_list.nextTree());
				}
				stream_param_list.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_prototype"


	public static class param_list_or_id_primitive_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "param_list_or_id_primitive_type"
	// smaliParser.g:606:1: param_list_or_id_primitive_type : PARAM_LIST_OR_ID_PRIMITIVE_TYPE -> PRIMITIVE_TYPE[$PARAM_LIST_OR_ID_PRIMITIVE_TYPE] ;
	public final smaliParser.param_list_or_id_primitive_type_return param_list_or_id_primitive_type() throws RecognitionException {
		smaliParser.param_list_or_id_primitive_type_return retval = new smaliParser.param_list_or_id_primitive_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PARAM_LIST_OR_ID_PRIMITIVE_TYPE97=null;

		CommonTree PARAM_LIST_OR_ID_PRIMITIVE_TYPE97_tree=null;
		RewriteRuleTokenStream stream_PARAM_LIST_OR_ID_PRIMITIVE_TYPE=new RewriteRuleTokenStream(adaptor,"token PARAM_LIST_OR_ID_PRIMITIVE_TYPE");

		try {
			// smaliParser.g:607:3: ( PARAM_LIST_OR_ID_PRIMITIVE_TYPE -> PRIMITIVE_TYPE[$PARAM_LIST_OR_ID_PRIMITIVE_TYPE] )
			// smaliParser.g:607:5: PARAM_LIST_OR_ID_PRIMITIVE_TYPE
			{
			PARAM_LIST_OR_ID_PRIMITIVE_TYPE97=(Token)match(input,PARAM_LIST_OR_ID_PRIMITIVE_TYPE,FOLLOW_PARAM_LIST_OR_ID_PRIMITIVE_TYPE_in_param_list_or_id_primitive_type2404);
			stream_PARAM_LIST_OR_ID_PRIMITIVE_TYPE.add(PARAM_LIST_OR_ID_PRIMITIVE_TYPE97);

			// AST REWRITE
			// elements:
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 607:37: -> PRIMITIVE_TYPE[$PARAM_LIST_OR_ID_PRIMITIVE_TYPE]
			{
				adaptor.addChild(root_0, (CommonTree)adaptor.create(PRIMITIVE_TYPE, PARAM_LIST_OR_ID_PRIMITIVE_TYPE97));
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "param_list_or_id_primitive_type"


	public static class param_list_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "param_list"
	// smaliParser.g:609:1: param_list : ( ( param_list_or_id_primitive_type )+ | ( nonvoid_type_descriptor )* );
	public final smaliParser.param_list_return param_list() throws RecognitionException {
		smaliParser.param_list_return retval = new smaliParser.param_list_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope param_list_or_id_primitive_type98 =null;
		ParserRuleReturnScope nonvoid_type_descriptor99 =null;


		try {
			// smaliParser.g:610:3: ( ( param_list_or_id_primitive_type )+ | ( nonvoid_type_descriptor )* )
			int alt14=2;
			int LA14_0 = input.LA(1);
			if ( (LA14_0==PARAM_LIST_OR_ID_PRIMITIVE_TYPE) ) {
				alt14=1;
			}
			else if ( (LA14_0==ARRAY_TYPE_PREFIX||LA14_0==CLASS_DESCRIPTOR||LA14_0==CLOSE_PAREN||LA14_0==PRIMITIVE_TYPE) ) {
				alt14=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 14, 0, input);
				throw nvae;
			}

			switch (alt14) {
				case 1 :
					// smaliParser.g:610:5: ( param_list_or_id_primitive_type )+
					{
					root_0 = (CommonTree)adaptor.nil();


					// smaliParser.g:610:5: ( param_list_or_id_primitive_type )+
					int cnt12=0;
					loop12:
					while (true) {
						int alt12=2;
						int LA12_0 = input.LA(1);
						if ( (LA12_0==PARAM_LIST_OR_ID_PRIMITIVE_TYPE) ) {
							alt12=1;
						}

						switch (alt12) {
						case 1 :
							// smaliParser.g:610:5: param_list_or_id_primitive_type
							{
							pushFollow(FOLLOW_param_list_or_id_primitive_type_in_param_list2419);
							param_list_or_id_primitive_type98=param_list_or_id_primitive_type();
							state._fsp--;

							adaptor.addChild(root_0, param_list_or_id_primitive_type98.getTree());

							}
							break;

						default :
							if ( cnt12 >= 1 ) break loop12;
							EarlyExitException eee = new EarlyExitException(12, input);
							throw eee;
						}
						cnt12++;
					}

					}
					break;
				case 2 :
					// smaliParser.g:611:5: ( nonvoid_type_descriptor )*
					{
					root_0 = (CommonTree)adaptor.nil();


					// smaliParser.g:611:5: ( nonvoid_type_descriptor )*
					loop13:
					while (true) {
						int alt13=2;
						int LA13_0 = input.LA(1);
						if ( (LA13_0==ARRAY_TYPE_PREFIX||LA13_0==CLASS_DESCRIPTOR||LA13_0==PRIMITIVE_TYPE) ) {
							alt13=1;
						}

						switch (alt13) {
						case 1 :
							// smaliParser.g:611:5: nonvoid_type_descriptor
							{
							pushFollow(FOLLOW_nonvoid_type_descriptor_in_param_list2426);
							nonvoid_type_descriptor99=nonvoid_type_descriptor();
							state._fsp--;

							adaptor.addChild(root_0, nonvoid_type_descriptor99.getTree());

							}
							break;

						default :
							break loop13;
						}
					}

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "param_list"


	public static class array_descriptor_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "array_descriptor"
	// smaliParser.g:613:1: array_descriptor : ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR ) ;
	public final smaliParser.array_descriptor_return array_descriptor() throws RecognitionException {
		smaliParser.array_descriptor_return retval = new smaliParser.array_descriptor_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ARRAY_TYPE_PREFIX100=null;
		Token set101=null;

		CommonTree ARRAY_TYPE_PREFIX100_tree=null;
		CommonTree set101_tree=null;

		try {
			// smaliParser.g:614:3: ( ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR ) )
			// smaliParser.g:614:5: ARRAY_TYPE_PREFIX ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR )
			{
			root_0 = (CommonTree)adaptor.nil();


			ARRAY_TYPE_PREFIX100=(Token)match(input,ARRAY_TYPE_PREFIX,FOLLOW_ARRAY_TYPE_PREFIX_in_array_descriptor2437);
			ARRAY_TYPE_PREFIX100_tree = (CommonTree)adaptor.create(ARRAY_TYPE_PREFIX100);
			adaptor.addChild(root_0, ARRAY_TYPE_PREFIX100_tree);

			set101=input.LT(1);
			if ( input.LA(1)==CLASS_DESCRIPTOR||input.LA(1)==PRIMITIVE_TYPE ) {
				input.consume();
				adaptor.addChild(root_0, (CommonTree)adaptor.create(set101));
				state.errorRecovery=false;
			}
			else {
				MismatchedSetException mse = new MismatchedSetException(null,input);
				throw mse;
			}
			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "array_descriptor"


	public static class type_descriptor_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "type_descriptor"
	// smaliParser.g:616:1: type_descriptor : ( VOID_TYPE | PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor );
	public final smaliParser.type_descriptor_return type_descriptor() throws RecognitionException {
		smaliParser.type_descriptor_return retval = new smaliParser.type_descriptor_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token VOID_TYPE102=null;
		Token PRIMITIVE_TYPE103=null;
		Token CLASS_DESCRIPTOR104=null;
		ParserRuleReturnScope array_descriptor105 =null;

		CommonTree VOID_TYPE102_tree=null;
		CommonTree PRIMITIVE_TYPE103_tree=null;
		CommonTree CLASS_DESCRIPTOR104_tree=null;

		try {
			// smaliParser.g:617:3: ( VOID_TYPE | PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor )
			int alt15=4;
			switch ( input.LA(1) ) {
			case VOID_TYPE:
				{
				alt15=1;
				}
				break;
			case PRIMITIVE_TYPE:
				{
				alt15=2;
				}
				break;
			case CLASS_DESCRIPTOR:
				{
				alt15=3;
				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				alt15=4;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 15, 0, input);
				throw nvae;
			}
			switch (alt15) {
				case 1 :
					// smaliParser.g:617:5: VOID_TYPE
					{
					root_0 = (CommonTree)adaptor.nil();


					VOID_TYPE102=(Token)match(input,VOID_TYPE,FOLLOW_VOID_TYPE_in_type_descriptor2455);
					VOID_TYPE102_tree = (CommonTree)adaptor.create(VOID_TYPE102);
					adaptor.addChild(root_0, VOID_TYPE102_tree);

					}
					break;
				case 2 :
					// smaliParser.g:618:5: PRIMITIVE_TYPE
					{
					root_0 = (CommonTree)adaptor.nil();


					PRIMITIVE_TYPE103=(Token)match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_type_descriptor2461);
					PRIMITIVE_TYPE103_tree = (CommonTree)adaptor.create(PRIMITIVE_TYPE103);
					adaptor.addChild(root_0, PRIMITIVE_TYPE103_tree);

					}
					break;
				case 3 :
					// smaliParser.g:619:5: CLASS_DESCRIPTOR
					{
					root_0 = (CommonTree)adaptor.nil();


					CLASS_DESCRIPTOR104=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_type_descriptor2467);
					CLASS_DESCRIPTOR104_tree = (CommonTree)adaptor.create(CLASS_DESCRIPTOR104);
					adaptor.addChild(root_0, CLASS_DESCRIPTOR104_tree);

					}
					break;
				case 4 :
					// smaliParser.g:620:5: array_descriptor
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_array_descriptor_in_type_descriptor2473);
					array_descriptor105=array_descriptor();
					state._fsp--;

					adaptor.addChild(root_0, array_descriptor105.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type_descriptor"


	public static class nonvoid_type_descriptor_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "nonvoid_type_descriptor"
	// smaliParser.g:622:1: nonvoid_type_descriptor : ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor );
	public final smaliParser.nonvoid_type_descriptor_return nonvoid_type_descriptor() throws RecognitionException {
		smaliParser.nonvoid_type_descriptor_return retval = new smaliParser.nonvoid_type_descriptor_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PRIMITIVE_TYPE106=null;
		Token CLASS_DESCRIPTOR107=null;
		ParserRuleReturnScope array_descriptor108 =null;

		CommonTree PRIMITIVE_TYPE106_tree=null;
		CommonTree CLASS_DESCRIPTOR107_tree=null;

		try {
			// smaliParser.g:623:3: ( PRIMITIVE_TYPE | CLASS_DESCRIPTOR | array_descriptor )
			int alt16=3;
			switch ( input.LA(1) ) {
			case PRIMITIVE_TYPE:
				{
				alt16=1;
				}
				break;
			case CLASS_DESCRIPTOR:
				{
				alt16=2;
				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				alt16=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 16, 0, input);
				throw nvae;
			}
			switch (alt16) {
				case 1 :
					// smaliParser.g:623:5: PRIMITIVE_TYPE
					{
					root_0 = (CommonTree)adaptor.nil();


					PRIMITIVE_TYPE106=(Token)match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_nonvoid_type_descriptor2483);
					PRIMITIVE_TYPE106_tree = (CommonTree)adaptor.create(PRIMITIVE_TYPE106);
					adaptor.addChild(root_0, PRIMITIVE_TYPE106_tree);

					}
					break;
				case 2 :
					// smaliParser.g:624:5: CLASS_DESCRIPTOR
					{
					root_0 = (CommonTree)adaptor.nil();


					CLASS_DESCRIPTOR107=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_nonvoid_type_descriptor2489);
					CLASS_DESCRIPTOR107_tree = (CommonTree)adaptor.create(CLASS_DESCRIPTOR107);
					adaptor.addChild(root_0, CLASS_DESCRIPTOR107_tree);

					}
					break;
				case 3 :
					// smaliParser.g:625:5: array_descriptor
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_array_descriptor_in_nonvoid_type_descriptor2495);
					array_descriptor108=array_descriptor();
					state._fsp--;

					adaptor.addChild(root_0, array_descriptor108.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "nonvoid_type_descriptor"


	public static class reference_type_descriptor_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "reference_type_descriptor"
	// smaliParser.g:627:1: reference_type_descriptor : ( CLASS_DESCRIPTOR | array_descriptor );
	public final smaliParser.reference_type_descriptor_return reference_type_descriptor() throws RecognitionException {
		smaliParser.reference_type_descriptor_return retval = new smaliParser.reference_type_descriptor_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CLASS_DESCRIPTOR109=null;
		ParserRuleReturnScope array_descriptor110 =null;

		CommonTree CLASS_DESCRIPTOR109_tree=null;

		try {
			// smaliParser.g:628:3: ( CLASS_DESCRIPTOR | array_descriptor )
			int alt17=2;
			int LA17_0 = input.LA(1);
			if ( (LA17_0==CLASS_DESCRIPTOR) ) {
				alt17=1;
			}
			else if ( (LA17_0==ARRAY_TYPE_PREFIX) ) {
				alt17=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 17, 0, input);
				throw nvae;
			}

			switch (alt17) {
				case 1 :
					// smaliParser.g:628:5: CLASS_DESCRIPTOR
					{
					root_0 = (CommonTree)adaptor.nil();


					CLASS_DESCRIPTOR109=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_reference_type_descriptor2505);
					CLASS_DESCRIPTOR109_tree = (CommonTree)adaptor.create(CLASS_DESCRIPTOR109);
					adaptor.addChild(root_0, CLASS_DESCRIPTOR109_tree);

					}
					break;
				case 2 :
					// smaliParser.g:629:5: array_descriptor
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_array_descriptor_in_reference_type_descriptor2511);
					array_descriptor110=array_descriptor();
					state._fsp--;

					adaptor.addChild(root_0, array_descriptor110.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "reference_type_descriptor"


	public static class integer_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "integer_literal"
	// smaliParser.g:631:1: integer_literal : ( POSITIVE_INTEGER_LITERAL -> INTEGER_LITERAL[$POSITIVE_INTEGER_LITERAL] | NEGATIVE_INTEGER_LITERAL -> INTEGER_LITERAL[$NEGATIVE_INTEGER_LITERAL] );
	public final smaliParser.integer_literal_return integer_literal() throws RecognitionException {
		smaliParser.integer_literal_return retval = new smaliParser.integer_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token POSITIVE_INTEGER_LITERAL111=null;
		Token NEGATIVE_INTEGER_LITERAL112=null;

		CommonTree POSITIVE_INTEGER_LITERAL111_tree=null;
		CommonTree NEGATIVE_INTEGER_LITERAL112_tree=null;
		RewriteRuleTokenStream stream_NEGATIVE_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token NEGATIVE_INTEGER_LITERAL");
		RewriteRuleTokenStream stream_POSITIVE_INTEGER_LITERAL=new RewriteRuleTokenStream(adaptor,"token POSITIVE_INTEGER_LITERAL");

		try {
			// smaliParser.g:632:3: ( POSITIVE_INTEGER_LITERAL -> INTEGER_LITERAL[$POSITIVE_INTEGER_LITERAL] | NEGATIVE_INTEGER_LITERAL -> INTEGER_LITERAL[$NEGATIVE_INTEGER_LITERAL] )
			int alt18=2;
			int LA18_0 = input.LA(1);
			if ( (LA18_0==POSITIVE_INTEGER_LITERAL) ) {
				alt18=1;
			}
			else if ( (LA18_0==NEGATIVE_INTEGER_LITERAL) ) {
				alt18=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 18, 0, input);
				throw nvae;
			}

			switch (alt18) {
				case 1 :
					// smaliParser.g:632:5: POSITIVE_INTEGER_LITERAL
					{
					POSITIVE_INTEGER_LITERAL111=(Token)match(input,POSITIVE_INTEGER_LITERAL,FOLLOW_POSITIVE_INTEGER_LITERAL_in_integer_literal2521);
					stream_POSITIVE_INTEGER_LITERAL.add(POSITIVE_INTEGER_LITERAL111);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 632:30: -> INTEGER_LITERAL[$POSITIVE_INTEGER_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INTEGER_LITERAL, POSITIVE_INTEGER_LITERAL111));
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:633:5: NEGATIVE_INTEGER_LITERAL
					{
					NEGATIVE_INTEGER_LITERAL112=(Token)match(input,NEGATIVE_INTEGER_LITERAL,FOLLOW_NEGATIVE_INTEGER_LITERAL_in_integer_literal2532);
					stream_NEGATIVE_INTEGER_LITERAL.add(NEGATIVE_INTEGER_LITERAL112);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 633:30: -> INTEGER_LITERAL[$NEGATIVE_INTEGER_LITERAL]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INTEGER_LITERAL, NEGATIVE_INTEGER_LITERAL112));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "integer_literal"


	public static class float_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "float_literal"
	// smaliParser.g:635:1: float_literal : ( FLOAT_LITERAL_OR_ID -> FLOAT_LITERAL[$FLOAT_LITERAL_OR_ID] | FLOAT_LITERAL );
	public final smaliParser.float_literal_return float_literal() throws RecognitionException {
		smaliParser.float_literal_return retval = new smaliParser.float_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token FLOAT_LITERAL_OR_ID113=null;
		Token FLOAT_LITERAL114=null;

		CommonTree FLOAT_LITERAL_OR_ID113_tree=null;
		CommonTree FLOAT_LITERAL114_tree=null;
		RewriteRuleTokenStream stream_FLOAT_LITERAL_OR_ID=new RewriteRuleTokenStream(adaptor,"token FLOAT_LITERAL_OR_ID");

		try {
			// smaliParser.g:636:3: ( FLOAT_LITERAL_OR_ID -> FLOAT_LITERAL[$FLOAT_LITERAL_OR_ID] | FLOAT_LITERAL )
			int alt19=2;
			int LA19_0 = input.LA(1);
			if ( (LA19_0==FLOAT_LITERAL_OR_ID) ) {
				alt19=1;
			}
			else if ( (LA19_0==FLOAT_LITERAL) ) {
				alt19=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 19, 0, input);
				throw nvae;
			}

			switch (alt19) {
				case 1 :
					// smaliParser.g:636:5: FLOAT_LITERAL_OR_ID
					{
					FLOAT_LITERAL_OR_ID113=(Token)match(input,FLOAT_LITERAL_OR_ID,FOLLOW_FLOAT_LITERAL_OR_ID_in_float_literal2547);
					stream_FLOAT_LITERAL_OR_ID.add(FLOAT_LITERAL_OR_ID113);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 636:25: -> FLOAT_LITERAL[$FLOAT_LITERAL_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(FLOAT_LITERAL, FLOAT_LITERAL_OR_ID113));
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:637:5: FLOAT_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					FLOAT_LITERAL114=(Token)match(input,FLOAT_LITERAL,FOLLOW_FLOAT_LITERAL_in_float_literal2558);
					FLOAT_LITERAL114_tree = (CommonTree)adaptor.create(FLOAT_LITERAL114);
					adaptor.addChild(root_0, FLOAT_LITERAL114_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "float_literal"


	public static class double_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "double_literal"
	// smaliParser.g:639:1: double_literal : ( DOUBLE_LITERAL_OR_ID -> DOUBLE_LITERAL[$DOUBLE_LITERAL_OR_ID] | DOUBLE_LITERAL );
	public final smaliParser.double_literal_return double_literal() throws RecognitionException {
		smaliParser.double_literal_return retval = new smaliParser.double_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token DOUBLE_LITERAL_OR_ID115=null;
		Token DOUBLE_LITERAL116=null;

		CommonTree DOUBLE_LITERAL_OR_ID115_tree=null;
		CommonTree DOUBLE_LITERAL116_tree=null;
		RewriteRuleTokenStream stream_DOUBLE_LITERAL_OR_ID=new RewriteRuleTokenStream(adaptor,"token DOUBLE_LITERAL_OR_ID");

		try {
			// smaliParser.g:640:3: ( DOUBLE_LITERAL_OR_ID -> DOUBLE_LITERAL[$DOUBLE_LITERAL_OR_ID] | DOUBLE_LITERAL )
			int alt20=2;
			int LA20_0 = input.LA(1);
			if ( (LA20_0==DOUBLE_LITERAL_OR_ID) ) {
				alt20=1;
			}
			else if ( (LA20_0==DOUBLE_LITERAL) ) {
				alt20=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 20, 0, input);
				throw nvae;
			}

			switch (alt20) {
				case 1 :
					// smaliParser.g:640:5: DOUBLE_LITERAL_OR_ID
					{
					DOUBLE_LITERAL_OR_ID115=(Token)match(input,DOUBLE_LITERAL_OR_ID,FOLLOW_DOUBLE_LITERAL_OR_ID_in_double_literal2568);
					stream_DOUBLE_LITERAL_OR_ID.add(DOUBLE_LITERAL_OR_ID115);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 640:26: -> DOUBLE_LITERAL[$DOUBLE_LITERAL_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(DOUBLE_LITERAL, DOUBLE_LITERAL_OR_ID115));
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:641:5: DOUBLE_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					DOUBLE_LITERAL116=(Token)match(input,DOUBLE_LITERAL,FOLLOW_DOUBLE_LITERAL_in_double_literal2579);
					DOUBLE_LITERAL116_tree = (CommonTree)adaptor.create(DOUBLE_LITERAL116);
					adaptor.addChild(root_0, DOUBLE_LITERAL116_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "double_literal"


	public static class literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "literal"
	// smaliParser.g:643:1: literal : ( LONG_LITERAL | integer_literal | SHORT_LITERAL | BYTE_LITERAL | float_literal | double_literal | CHAR_LITERAL | STRING_LITERAL | BOOL_LITERAL | NULL_LITERAL | array_literal | subannotation | type_field_method_literal | enum_literal | method_handle_literal | method_prototype );
	public final smaliParser.literal_return literal() throws RecognitionException {
		smaliParser.literal_return retval = new smaliParser.literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LONG_LITERAL117=null;
		Token SHORT_LITERAL119=null;
		Token BYTE_LITERAL120=null;
		Token CHAR_LITERAL123=null;
		Token STRING_LITERAL124=null;
		Token BOOL_LITERAL125=null;
		Token NULL_LITERAL126=null;
		ParserRuleReturnScope integer_literal118 =null;
		ParserRuleReturnScope float_literal121 =null;
		ParserRuleReturnScope double_literal122 =null;
		ParserRuleReturnScope array_literal127 =null;
		ParserRuleReturnScope subannotation128 =null;
		ParserRuleReturnScope type_field_method_literal129 =null;
		ParserRuleReturnScope enum_literal130 =null;
		ParserRuleReturnScope method_handle_literal131 =null;
		ParserRuleReturnScope method_prototype132 =null;

		CommonTree LONG_LITERAL117_tree=null;
		CommonTree SHORT_LITERAL119_tree=null;
		CommonTree BYTE_LITERAL120_tree=null;
		CommonTree CHAR_LITERAL123_tree=null;
		CommonTree STRING_LITERAL124_tree=null;
		CommonTree BOOL_LITERAL125_tree=null;
		CommonTree NULL_LITERAL126_tree=null;

		try {
			// smaliParser.g:644:3: ( LONG_LITERAL | integer_literal | SHORT_LITERAL | BYTE_LITERAL | float_literal | double_literal | CHAR_LITERAL | STRING_LITERAL | BOOL_LITERAL | NULL_LITERAL | array_literal | subannotation | type_field_method_literal | enum_literal | method_handle_literal | method_prototype )
			int alt21=16;
			switch ( input.LA(1) ) {
			case LONG_LITERAL:
				{
				alt21=1;
				}
				break;
			case POSITIVE_INTEGER_LITERAL:
				{
				int LA21_2 = input.LA(2);
				if ( (LA21_2==EOF||(LA21_2 >= ACCESS_SPEC && LA21_2 <= ANNOTATION_VISIBILITY)||LA21_2==BOOL_LITERAL||(LA21_2 >= CLASS_DIRECTIVE && LA21_2 <= CLOSE_PAREN)||LA21_2==COMMA||(LA21_2 >= DOUBLE_LITERAL_OR_ID && LA21_2 <= END_ANNOTATION_DIRECTIVE)||LA21_2==END_FIELD_DIRECTIVE||LA21_2==END_SUBANNOTATION_DIRECTIVE||LA21_2==FIELD_DIRECTIVE||(LA21_2 >= FLOAT_LITERAL_OR_ID && LA21_2 <= IMPLEMENTS_DIRECTIVE)||(LA21_2 >= INSTRUCTION_FORMAT10t && LA21_2 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_2==INSTRUCTION_FORMAT11x||LA21_2==INSTRUCTION_FORMAT12x_OR_ID||(LA21_2 >= INSTRUCTION_FORMAT21c_FIELD && LA21_2 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_2==INSTRUCTION_FORMAT21t||(LA21_2 >= INSTRUCTION_FORMAT22c_FIELD && LA21_2 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_2 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_2 <= INSTRUCTION_FORMAT22t)||LA21_2==INSTRUCTION_FORMAT23x||(LA21_2 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_2 <= INSTRUCTION_FORMAT31t)||(LA21_2 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_2 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_2 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_2 <= INSTRUCTION_FORMAT51l)||(LA21_2 >= METHOD_DIRECTIVE && LA21_2 <= NULL_LITERAL)||(LA21_2 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_2 <= PRIMITIVE_TYPE)||LA21_2==REGISTER||(LA21_2 >= SIMPLE_NAME && LA21_2 <= SOURCE_DIRECTIVE)||(LA21_2 >= SUPER_DIRECTIVE && LA21_2 <= VOID_TYPE)) ) {
					alt21=2;
				}
				else if ( (LA21_2==COLON||LA21_2==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NEGATIVE_INTEGER_LITERAL:
				{
				int LA21_3 = input.LA(2);
				if ( (LA21_3==EOF||(LA21_3 >= ACCESS_SPEC && LA21_3 <= ANNOTATION_VISIBILITY)||LA21_3==BOOL_LITERAL||(LA21_3 >= CLASS_DIRECTIVE && LA21_3 <= CLOSE_PAREN)||LA21_3==COMMA||(LA21_3 >= DOUBLE_LITERAL_OR_ID && LA21_3 <= END_ANNOTATION_DIRECTIVE)||LA21_3==END_FIELD_DIRECTIVE||LA21_3==END_SUBANNOTATION_DIRECTIVE||LA21_3==FIELD_DIRECTIVE||(LA21_3 >= FLOAT_LITERAL_OR_ID && LA21_3 <= IMPLEMENTS_DIRECTIVE)||(LA21_3 >= INSTRUCTION_FORMAT10t && LA21_3 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_3==INSTRUCTION_FORMAT11x||LA21_3==INSTRUCTION_FORMAT12x_OR_ID||(LA21_3 >= INSTRUCTION_FORMAT21c_FIELD && LA21_3 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_3==INSTRUCTION_FORMAT21t||(LA21_3 >= INSTRUCTION_FORMAT22c_FIELD && LA21_3 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_3 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_3 <= INSTRUCTION_FORMAT22t)||LA21_3==INSTRUCTION_FORMAT23x||(LA21_3 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_3 <= INSTRUCTION_FORMAT31t)||(LA21_3 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_3 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_3 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_3 <= INSTRUCTION_FORMAT51l)||(LA21_3 >= METHOD_DIRECTIVE && LA21_3 <= NULL_LITERAL)||(LA21_3 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_3 <= PRIMITIVE_TYPE)||LA21_3==REGISTER||(LA21_3 >= SIMPLE_NAME && LA21_3 <= SOURCE_DIRECTIVE)||(LA21_3 >= SUPER_DIRECTIVE && LA21_3 <= VOID_TYPE)) ) {
					alt21=2;
				}
				else if ( (LA21_3==COLON||LA21_3==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 3, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case SHORT_LITERAL:
				{
				alt21=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt21=4;
				}
				break;
			case FLOAT_LITERAL_OR_ID:
				{
				int LA21_6 = input.LA(2);
				if ( (LA21_6==EOF||(LA21_6 >= ACCESS_SPEC && LA21_6 <= ANNOTATION_VISIBILITY)||LA21_6==BOOL_LITERAL||(LA21_6 >= CLASS_DIRECTIVE && LA21_6 <= CLOSE_PAREN)||LA21_6==COMMA||(LA21_6 >= DOUBLE_LITERAL_OR_ID && LA21_6 <= END_ANNOTATION_DIRECTIVE)||LA21_6==END_FIELD_DIRECTIVE||LA21_6==END_SUBANNOTATION_DIRECTIVE||LA21_6==FIELD_DIRECTIVE||(LA21_6 >= FLOAT_LITERAL_OR_ID && LA21_6 <= IMPLEMENTS_DIRECTIVE)||(LA21_6 >= INSTRUCTION_FORMAT10t && LA21_6 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_6==INSTRUCTION_FORMAT11x||LA21_6==INSTRUCTION_FORMAT12x_OR_ID||(LA21_6 >= INSTRUCTION_FORMAT21c_FIELD && LA21_6 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_6==INSTRUCTION_FORMAT21t||(LA21_6 >= INSTRUCTION_FORMAT22c_FIELD && LA21_6 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_6 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_6 <= INSTRUCTION_FORMAT22t)||LA21_6==INSTRUCTION_FORMAT23x||(LA21_6 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_6 <= INSTRUCTION_FORMAT31t)||(LA21_6 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_6 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_6 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_6 <= INSTRUCTION_FORMAT51l)||(LA21_6 >= METHOD_DIRECTIVE && LA21_6 <= NULL_LITERAL)||(LA21_6 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_6 <= PRIMITIVE_TYPE)||LA21_6==REGISTER||(LA21_6 >= SIMPLE_NAME && LA21_6 <= SOURCE_DIRECTIVE)||(LA21_6 >= SUPER_DIRECTIVE && LA21_6 <= VOID_TYPE)) ) {
					alt21=5;
				}
				else if ( (LA21_6==COLON||LA21_6==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 6, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case FLOAT_LITERAL:
				{
				alt21=5;
				}
				break;
			case DOUBLE_LITERAL_OR_ID:
				{
				int LA21_8 = input.LA(2);
				if ( (LA21_8==EOF||(LA21_8 >= ACCESS_SPEC && LA21_8 <= ANNOTATION_VISIBILITY)||LA21_8==BOOL_LITERAL||(LA21_8 >= CLASS_DIRECTIVE && LA21_8 <= CLOSE_PAREN)||LA21_8==COMMA||(LA21_8 >= DOUBLE_LITERAL_OR_ID && LA21_8 <= END_ANNOTATION_DIRECTIVE)||LA21_8==END_FIELD_DIRECTIVE||LA21_8==END_SUBANNOTATION_DIRECTIVE||LA21_8==FIELD_DIRECTIVE||(LA21_8 >= FLOAT_LITERAL_OR_ID && LA21_8 <= IMPLEMENTS_DIRECTIVE)||(LA21_8 >= INSTRUCTION_FORMAT10t && LA21_8 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_8==INSTRUCTION_FORMAT11x||LA21_8==INSTRUCTION_FORMAT12x_OR_ID||(LA21_8 >= INSTRUCTION_FORMAT21c_FIELD && LA21_8 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_8==INSTRUCTION_FORMAT21t||(LA21_8 >= INSTRUCTION_FORMAT22c_FIELD && LA21_8 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_8 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_8 <= INSTRUCTION_FORMAT22t)||LA21_8==INSTRUCTION_FORMAT23x||(LA21_8 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_8 <= INSTRUCTION_FORMAT31t)||(LA21_8 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_8 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_8 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_8 <= INSTRUCTION_FORMAT51l)||(LA21_8 >= METHOD_DIRECTIVE && LA21_8 <= NULL_LITERAL)||(LA21_8 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_8 <= PRIMITIVE_TYPE)||LA21_8==REGISTER||(LA21_8 >= SIMPLE_NAME && LA21_8 <= SOURCE_DIRECTIVE)||(LA21_8 >= SUPER_DIRECTIVE && LA21_8 <= VOID_TYPE)) ) {
					alt21=6;
				}
				else if ( (LA21_8==COLON||LA21_8==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 8, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case DOUBLE_LITERAL:
				{
				alt21=6;
				}
				break;
			case CHAR_LITERAL:
				{
				alt21=7;
				}
				break;
			case STRING_LITERAL:
				{
				alt21=8;
				}
				break;
			case BOOL_LITERAL:
				{
				int LA21_12 = input.LA(2);
				if ( (LA21_12==EOF||(LA21_12 >= ACCESS_SPEC && LA21_12 <= ANNOTATION_VISIBILITY)||LA21_12==BOOL_LITERAL||(LA21_12 >= CLASS_DIRECTIVE && LA21_12 <= CLOSE_PAREN)||LA21_12==COMMA||(LA21_12 >= DOUBLE_LITERAL_OR_ID && LA21_12 <= END_ANNOTATION_DIRECTIVE)||LA21_12==END_FIELD_DIRECTIVE||LA21_12==END_SUBANNOTATION_DIRECTIVE||LA21_12==FIELD_DIRECTIVE||(LA21_12 >= FLOAT_LITERAL_OR_ID && LA21_12 <= IMPLEMENTS_DIRECTIVE)||(LA21_12 >= INSTRUCTION_FORMAT10t && LA21_12 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_12==INSTRUCTION_FORMAT11x||LA21_12==INSTRUCTION_FORMAT12x_OR_ID||(LA21_12 >= INSTRUCTION_FORMAT21c_FIELD && LA21_12 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_12==INSTRUCTION_FORMAT21t||(LA21_12 >= INSTRUCTION_FORMAT22c_FIELD && LA21_12 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_12 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_12 <= INSTRUCTION_FORMAT22t)||LA21_12==INSTRUCTION_FORMAT23x||(LA21_12 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_12 <= INSTRUCTION_FORMAT31t)||(LA21_12 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_12 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_12 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_12 <= INSTRUCTION_FORMAT51l)||(LA21_12 >= METHOD_DIRECTIVE && LA21_12 <= NULL_LITERAL)||(LA21_12 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_12 <= PRIMITIVE_TYPE)||LA21_12==REGISTER||(LA21_12 >= SIMPLE_NAME && LA21_12 <= SOURCE_DIRECTIVE)||(LA21_12 >= SUPER_DIRECTIVE && LA21_12 <= VOID_TYPE)) ) {
					alt21=9;
				}
				else if ( (LA21_12==COLON||LA21_12==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 12, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case NULL_LITERAL:
				{
				int LA21_13 = input.LA(2);
				if ( (LA21_13==EOF||(LA21_13 >= ACCESS_SPEC && LA21_13 <= ANNOTATION_VISIBILITY)||LA21_13==BOOL_LITERAL||(LA21_13 >= CLASS_DIRECTIVE && LA21_13 <= CLOSE_PAREN)||LA21_13==COMMA||(LA21_13 >= DOUBLE_LITERAL_OR_ID && LA21_13 <= END_ANNOTATION_DIRECTIVE)||LA21_13==END_FIELD_DIRECTIVE||LA21_13==END_SUBANNOTATION_DIRECTIVE||LA21_13==FIELD_DIRECTIVE||(LA21_13 >= FLOAT_LITERAL_OR_ID && LA21_13 <= IMPLEMENTS_DIRECTIVE)||(LA21_13 >= INSTRUCTION_FORMAT10t && LA21_13 <= INSTRUCTION_FORMAT10x_ODEX)||LA21_13==INSTRUCTION_FORMAT11x||LA21_13==INSTRUCTION_FORMAT12x_OR_ID||(LA21_13 >= INSTRUCTION_FORMAT21c_FIELD && LA21_13 <= INSTRUCTION_FORMAT21c_TYPE)||LA21_13==INSTRUCTION_FORMAT21t||(LA21_13 >= INSTRUCTION_FORMAT22c_FIELD && LA21_13 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA21_13 >= INSTRUCTION_FORMAT22s_OR_ID && LA21_13 <= INSTRUCTION_FORMAT22t)||LA21_13==INSTRUCTION_FORMAT23x||(LA21_13 >= INSTRUCTION_FORMAT31i_OR_ID && LA21_13 <= INSTRUCTION_FORMAT31t)||(LA21_13 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA21_13 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA21_13 >= INSTRUCTION_FORMAT45cc_METHOD && LA21_13 <= INSTRUCTION_FORMAT51l)||(LA21_13 >= METHOD_DIRECTIVE && LA21_13 <= NULL_LITERAL)||(LA21_13 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA21_13 <= PRIMITIVE_TYPE)||LA21_13==REGISTER||(LA21_13 >= SIMPLE_NAME && LA21_13 <= SOURCE_DIRECTIVE)||(LA21_13 >= SUPER_DIRECTIVE && LA21_13 <= VOID_TYPE)) ) {
					alt21=10;
				}
				else if ( (LA21_13==COLON||LA21_13==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 13, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case OPEN_BRACE:
				{
				alt21=11;
				}
				break;
			case SUBANNOTATION_DIRECTIVE:
				{
				alt21=12;
				}
				break;
			case ACCESS_SPEC:
			case ANNOTATION_VISIBILITY:
			case ARRAY_TYPE_PREFIX:
			case CLASS_DESCRIPTOR:
			case INSTRUCTION_FORMAT10t:
			case INSTRUCTION_FORMAT10x:
			case INSTRUCTION_FORMAT10x_ODEX:
			case INSTRUCTION_FORMAT11x:
			case INSTRUCTION_FORMAT12x_OR_ID:
			case INSTRUCTION_FORMAT21c_FIELD:
			case INSTRUCTION_FORMAT21c_FIELD_ODEX:
			case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
			case INSTRUCTION_FORMAT21c_METHOD_TYPE:
			case INSTRUCTION_FORMAT21c_STRING:
			case INSTRUCTION_FORMAT21c_TYPE:
			case INSTRUCTION_FORMAT21t:
			case INSTRUCTION_FORMAT22c_FIELD:
			case INSTRUCTION_FORMAT22c_FIELD_ODEX:
			case INSTRUCTION_FORMAT22c_TYPE:
			case INSTRUCTION_FORMAT22cs_FIELD:
			case INSTRUCTION_FORMAT22s_OR_ID:
			case INSTRUCTION_FORMAT22t:
			case INSTRUCTION_FORMAT23x:
			case INSTRUCTION_FORMAT31i_OR_ID:
			case INSTRUCTION_FORMAT31t:
			case INSTRUCTION_FORMAT35c_CALL_SITE:
			case INSTRUCTION_FORMAT35c_METHOD:
			case INSTRUCTION_FORMAT35c_METHOD_ODEX:
			case INSTRUCTION_FORMAT35c_TYPE:
			case INSTRUCTION_FORMAT35mi_METHOD:
			case INSTRUCTION_FORMAT35ms_METHOD:
			case INSTRUCTION_FORMAT45cc_METHOD:
			case INSTRUCTION_FORMAT4rcc_METHOD:
			case INSTRUCTION_FORMAT51l:
			case MEMBER_NAME:
			case PARAM_LIST_OR_ID_PRIMITIVE_TYPE:
			case PRIMITIVE_TYPE:
			case REGISTER:
			case SIMPLE_NAME:
			case VERIFICATION_ERROR_TYPE:
			case VOID_TYPE:
				{
				alt21=13;
				}
				break;
			case METHOD_HANDLE_TYPE_FIELD:
				{
				int LA21_17 = input.LA(2);
				if ( (LA21_17==AT) ) {
					alt21=15;
				}
				else if ( (LA21_17==COLON||LA21_17==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 17, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case METHOD_HANDLE_TYPE_METHOD:
				{
				int LA21_18 = input.LA(2);
				if ( (LA21_18==AT) ) {
					alt21=15;
				}
				else if ( (LA21_18==COLON||LA21_18==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 18, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
				{
				int LA21_19 = input.LA(2);
				if ( (LA21_19==AT) ) {
					alt21=15;
				}
				else if ( (LA21_19==COLON||LA21_19==OPEN_PAREN) ) {
					alt21=13;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 21, 19, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ENUM_DIRECTIVE:
				{
				alt21=14;
				}
				break;
			case OPEN_PAREN:
				{
				alt21=16;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 21, 0, input);
				throw nvae;
			}
			switch (alt21) {
				case 1 :
					// smaliParser.g:644:5: LONG_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					LONG_LITERAL117=(Token)match(input,LONG_LITERAL,FOLLOW_LONG_LITERAL_in_literal2589);
					LONG_LITERAL117_tree = (CommonTree)adaptor.create(LONG_LITERAL117);
					adaptor.addChild(root_0, LONG_LITERAL117_tree);

					}
					break;
				case 2 :
					// smaliParser.g:645:5: integer_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_integer_literal_in_literal2595);
					integer_literal118=integer_literal();
					state._fsp--;

					adaptor.addChild(root_0, integer_literal118.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:646:5: SHORT_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORT_LITERAL119=(Token)match(input,SHORT_LITERAL,FOLLOW_SHORT_LITERAL_in_literal2601);
					SHORT_LITERAL119_tree = (CommonTree)adaptor.create(SHORT_LITERAL119);
					adaptor.addChild(root_0, SHORT_LITERAL119_tree);

					}
					break;
				case 4 :
					// smaliParser.g:647:5: BYTE_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BYTE_LITERAL120=(Token)match(input,BYTE_LITERAL,FOLLOW_BYTE_LITERAL_in_literal2607);
					BYTE_LITERAL120_tree = (CommonTree)adaptor.create(BYTE_LITERAL120);
					adaptor.addChild(root_0, BYTE_LITERAL120_tree);

					}
					break;
				case 5 :
					// smaliParser.g:648:5: float_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_float_literal_in_literal2613);
					float_literal121=float_literal();
					state._fsp--;

					adaptor.addChild(root_0, float_literal121.getTree());

					}
					break;
				case 6 :
					// smaliParser.g:649:5: double_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_double_literal_in_literal2619);
					double_literal122=double_literal();
					state._fsp--;

					adaptor.addChild(root_0, double_literal122.getTree());

					}
					break;
				case 7 :
					// smaliParser.g:650:5: CHAR_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					CHAR_LITERAL123=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_literal2625);
					CHAR_LITERAL123_tree = (CommonTree)adaptor.create(CHAR_LITERAL123);
					adaptor.addChild(root_0, CHAR_LITERAL123_tree);

					}
					break;
				case 8 :
					// smaliParser.g:651:5: STRING_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					STRING_LITERAL124=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_literal2631);
					STRING_LITERAL124_tree = (CommonTree)adaptor.create(STRING_LITERAL124);
					adaptor.addChild(root_0, STRING_LITERAL124_tree);

					}
					break;
				case 9 :
					// smaliParser.g:652:5: BOOL_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOL_LITERAL125=(Token)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_literal2637);
					BOOL_LITERAL125_tree = (CommonTree)adaptor.create(BOOL_LITERAL125);
					adaptor.addChild(root_0, BOOL_LITERAL125_tree);

					}
					break;
				case 10 :
					// smaliParser.g:653:5: NULL_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					NULL_LITERAL126=(Token)match(input,NULL_LITERAL,FOLLOW_NULL_LITERAL_in_literal2643);
					NULL_LITERAL126_tree = (CommonTree)adaptor.create(NULL_LITERAL126);
					adaptor.addChild(root_0, NULL_LITERAL126_tree);

					}
					break;
				case 11 :
					// smaliParser.g:654:5: array_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_array_literal_in_literal2649);
					array_literal127=array_literal();
					state._fsp--;

					adaptor.addChild(root_0, array_literal127.getTree());

					}
					break;
				case 12 :
					// smaliParser.g:655:5: subannotation
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_subannotation_in_literal2655);
					subannotation128=subannotation();
					state._fsp--;

					adaptor.addChild(root_0, subannotation128.getTree());

					}
					break;
				case 13 :
					// smaliParser.g:656:5: type_field_method_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_type_field_method_literal_in_literal2661);
					type_field_method_literal129=type_field_method_literal();
					state._fsp--;

					adaptor.addChild(root_0, type_field_method_literal129.getTree());

					}
					break;
				case 14 :
					// smaliParser.g:657:5: enum_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_enum_literal_in_literal2667);
					enum_literal130=enum_literal();
					state._fsp--;

					adaptor.addChild(root_0, enum_literal130.getTree());

					}
					break;
				case 15 :
					// smaliParser.g:658:5: method_handle_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_method_handle_literal_in_literal2673);
					method_handle_literal131=method_handle_literal();
					state._fsp--;

					adaptor.addChild(root_0, method_handle_literal131.getTree());

					}
					break;
				case 16 :
					// smaliParser.g:659:5: method_prototype
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_method_prototype_in_literal2679);
					method_prototype132=method_prototype();
					state._fsp--;

					adaptor.addChild(root_0, method_prototype132.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "literal"


	public static class parsed_integer_literal_return extends ParserRuleReturnScope {
		public int value;
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parsed_integer_literal"
	// smaliParser.g:661:1: parsed_integer_literal returns [int value] : integer_literal ;
	public final smaliParser.parsed_integer_literal_return parsed_integer_literal() throws RecognitionException {
		smaliParser.parsed_integer_literal_return retval = new smaliParser.parsed_integer_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope integer_literal133 =null;


		try {
			// smaliParser.g:662:3: ( integer_literal )
			// smaliParser.g:662:5: integer_literal
			{
			root_0 = (CommonTree)adaptor.nil();


			pushFollow(FOLLOW_integer_literal_in_parsed_integer_literal2692);
			integer_literal133=integer_literal();
			state._fsp--;

			adaptor.addChild(root_0, integer_literal133.getTree());

			 retval.value = LiteralTools.parseInt((integer_literal133!=null?input.toString(integer_literal133.start,integer_literal133.stop):null));
			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parsed_integer_literal"


	public static class integral_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "integral_literal"
	// smaliParser.g:664:1: integral_literal : ( LONG_LITERAL | integer_literal | SHORT_LITERAL | CHAR_LITERAL | BYTE_LITERAL );
	public final smaliParser.integral_literal_return integral_literal() throws RecognitionException {
		smaliParser.integral_literal_return retval = new smaliParser.integral_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LONG_LITERAL134=null;
		Token SHORT_LITERAL136=null;
		Token CHAR_LITERAL137=null;
		Token BYTE_LITERAL138=null;
		ParserRuleReturnScope integer_literal135 =null;

		CommonTree LONG_LITERAL134_tree=null;
		CommonTree SHORT_LITERAL136_tree=null;
		CommonTree CHAR_LITERAL137_tree=null;
		CommonTree BYTE_LITERAL138_tree=null;

		try {
			// smaliParser.g:665:3: ( LONG_LITERAL | integer_literal | SHORT_LITERAL | CHAR_LITERAL | BYTE_LITERAL )
			int alt22=5;
			switch ( input.LA(1) ) {
			case LONG_LITERAL:
				{
				alt22=1;
				}
				break;
			case NEGATIVE_INTEGER_LITERAL:
			case POSITIVE_INTEGER_LITERAL:
				{
				alt22=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt22=3;
				}
				break;
			case CHAR_LITERAL:
				{
				alt22=4;
				}
				break;
			case BYTE_LITERAL:
				{
				alt22=5;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 22, 0, input);
				throw nvae;
			}
			switch (alt22) {
				case 1 :
					// smaliParser.g:665:5: LONG_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					LONG_LITERAL134=(Token)match(input,LONG_LITERAL,FOLLOW_LONG_LITERAL_in_integral_literal2704);
					LONG_LITERAL134_tree = (CommonTree)adaptor.create(LONG_LITERAL134);
					adaptor.addChild(root_0, LONG_LITERAL134_tree);

					}
					break;
				case 2 :
					// smaliParser.g:666:5: integer_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_integer_literal_in_integral_literal2710);
					integer_literal135=integer_literal();
					state._fsp--;

					adaptor.addChild(root_0, integer_literal135.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:667:5: SHORT_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORT_LITERAL136=(Token)match(input,SHORT_LITERAL,FOLLOW_SHORT_LITERAL_in_integral_literal2716);
					SHORT_LITERAL136_tree = (CommonTree)adaptor.create(SHORT_LITERAL136);
					adaptor.addChild(root_0, SHORT_LITERAL136_tree);

					}
					break;
				case 4 :
					// smaliParser.g:668:5: CHAR_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					CHAR_LITERAL137=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_integral_literal2722);
					CHAR_LITERAL137_tree = (CommonTree)adaptor.create(CHAR_LITERAL137);
					adaptor.addChild(root_0, CHAR_LITERAL137_tree);

					}
					break;
				case 5 :
					// smaliParser.g:669:5: BYTE_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BYTE_LITERAL138=(Token)match(input,BYTE_LITERAL,FOLLOW_BYTE_LITERAL_in_integral_literal2728);
					BYTE_LITERAL138_tree = (CommonTree)adaptor.create(BYTE_LITERAL138);
					adaptor.addChild(root_0, BYTE_LITERAL138_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "integral_literal"


	public static class fixed_32bit_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "fixed_32bit_literal"
	// smaliParser.g:671:1: fixed_32bit_literal : ( LONG_LITERAL | integer_literal | SHORT_LITERAL | BYTE_LITERAL | float_literal | CHAR_LITERAL | BOOL_LITERAL );
	public final smaliParser.fixed_32bit_literal_return fixed_32bit_literal() throws RecognitionException {
		smaliParser.fixed_32bit_literal_return retval = new smaliParser.fixed_32bit_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LONG_LITERAL139=null;
		Token SHORT_LITERAL141=null;
		Token BYTE_LITERAL142=null;
		Token CHAR_LITERAL144=null;
		Token BOOL_LITERAL145=null;
		ParserRuleReturnScope integer_literal140 =null;
		ParserRuleReturnScope float_literal143 =null;

		CommonTree LONG_LITERAL139_tree=null;
		CommonTree SHORT_LITERAL141_tree=null;
		CommonTree BYTE_LITERAL142_tree=null;
		CommonTree CHAR_LITERAL144_tree=null;
		CommonTree BOOL_LITERAL145_tree=null;

		try {
			// smaliParser.g:672:3: ( LONG_LITERAL | integer_literal | SHORT_LITERAL | BYTE_LITERAL | float_literal | CHAR_LITERAL | BOOL_LITERAL )
			int alt23=7;
			switch ( input.LA(1) ) {
			case LONG_LITERAL:
				{
				alt23=1;
				}
				break;
			case NEGATIVE_INTEGER_LITERAL:
			case POSITIVE_INTEGER_LITERAL:
				{
				alt23=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt23=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt23=4;
				}
				break;
			case FLOAT_LITERAL:
			case FLOAT_LITERAL_OR_ID:
				{
				alt23=5;
				}
				break;
			case CHAR_LITERAL:
				{
				alt23=6;
				}
				break;
			case BOOL_LITERAL:
				{
				alt23=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 23, 0, input);
				throw nvae;
			}
			switch (alt23) {
				case 1 :
					// smaliParser.g:672:5: LONG_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					LONG_LITERAL139=(Token)match(input,LONG_LITERAL,FOLLOW_LONG_LITERAL_in_fixed_32bit_literal2738);
					LONG_LITERAL139_tree = (CommonTree)adaptor.create(LONG_LITERAL139);
					adaptor.addChild(root_0, LONG_LITERAL139_tree);

					}
					break;
				case 2 :
					// smaliParser.g:673:5: integer_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_integer_literal_in_fixed_32bit_literal2744);
					integer_literal140=integer_literal();
					state._fsp--;

					adaptor.addChild(root_0, integer_literal140.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:674:5: SHORT_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORT_LITERAL141=(Token)match(input,SHORT_LITERAL,FOLLOW_SHORT_LITERAL_in_fixed_32bit_literal2750);
					SHORT_LITERAL141_tree = (CommonTree)adaptor.create(SHORT_LITERAL141);
					adaptor.addChild(root_0, SHORT_LITERAL141_tree);

					}
					break;
				case 4 :
					// smaliParser.g:675:5: BYTE_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BYTE_LITERAL142=(Token)match(input,BYTE_LITERAL,FOLLOW_BYTE_LITERAL_in_fixed_32bit_literal2756);
					BYTE_LITERAL142_tree = (CommonTree)adaptor.create(BYTE_LITERAL142);
					adaptor.addChild(root_0, BYTE_LITERAL142_tree);

					}
					break;
				case 5 :
					// smaliParser.g:676:5: float_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_float_literal_in_fixed_32bit_literal2762);
					float_literal143=float_literal();
					state._fsp--;

					adaptor.addChild(root_0, float_literal143.getTree());

					}
					break;
				case 6 :
					// smaliParser.g:677:5: CHAR_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					CHAR_LITERAL144=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_fixed_32bit_literal2768);
					CHAR_LITERAL144_tree = (CommonTree)adaptor.create(CHAR_LITERAL144);
					adaptor.addChild(root_0, CHAR_LITERAL144_tree);

					}
					break;
				case 7 :
					// smaliParser.g:678:5: BOOL_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOL_LITERAL145=(Token)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_fixed_32bit_literal2774);
					BOOL_LITERAL145_tree = (CommonTree)adaptor.create(BOOL_LITERAL145);
					adaptor.addChild(root_0, BOOL_LITERAL145_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fixed_32bit_literal"


	public static class fixed_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "fixed_literal"
	// smaliParser.g:680:1: fixed_literal : ( integer_literal | LONG_LITERAL | SHORT_LITERAL | BYTE_LITERAL | float_literal | double_literal | CHAR_LITERAL | BOOL_LITERAL );
	public final smaliParser.fixed_literal_return fixed_literal() throws RecognitionException {
		smaliParser.fixed_literal_return retval = new smaliParser.fixed_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LONG_LITERAL147=null;
		Token SHORT_LITERAL148=null;
		Token BYTE_LITERAL149=null;
		Token CHAR_LITERAL152=null;
		Token BOOL_LITERAL153=null;
		ParserRuleReturnScope integer_literal146 =null;
		ParserRuleReturnScope float_literal150 =null;
		ParserRuleReturnScope double_literal151 =null;

		CommonTree LONG_LITERAL147_tree=null;
		CommonTree SHORT_LITERAL148_tree=null;
		CommonTree BYTE_LITERAL149_tree=null;
		CommonTree CHAR_LITERAL152_tree=null;
		CommonTree BOOL_LITERAL153_tree=null;

		try {
			// smaliParser.g:681:3: ( integer_literal | LONG_LITERAL | SHORT_LITERAL | BYTE_LITERAL | float_literal | double_literal | CHAR_LITERAL | BOOL_LITERAL )
			int alt24=8;
			switch ( input.LA(1) ) {
			case NEGATIVE_INTEGER_LITERAL:
			case POSITIVE_INTEGER_LITERAL:
				{
				alt24=1;
				}
				break;
			case LONG_LITERAL:
				{
				alt24=2;
				}
				break;
			case SHORT_LITERAL:
				{
				alt24=3;
				}
				break;
			case BYTE_LITERAL:
				{
				alt24=4;
				}
				break;
			case FLOAT_LITERAL:
			case FLOAT_LITERAL_OR_ID:
				{
				alt24=5;
				}
				break;
			case DOUBLE_LITERAL:
			case DOUBLE_LITERAL_OR_ID:
				{
				alt24=6;
				}
				break;
			case CHAR_LITERAL:
				{
				alt24=7;
				}
				break;
			case BOOL_LITERAL:
				{
				alt24=8;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 24, 0, input);
				throw nvae;
			}
			switch (alt24) {
				case 1 :
					// smaliParser.g:681:5: integer_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_integer_literal_in_fixed_literal2784);
					integer_literal146=integer_literal();
					state._fsp--;

					adaptor.addChild(root_0, integer_literal146.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:682:5: LONG_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					LONG_LITERAL147=(Token)match(input,LONG_LITERAL,FOLLOW_LONG_LITERAL_in_fixed_literal2790);
					LONG_LITERAL147_tree = (CommonTree)adaptor.create(LONG_LITERAL147);
					adaptor.addChild(root_0, LONG_LITERAL147_tree);

					}
					break;
				case 3 :
					// smaliParser.g:683:5: SHORT_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					SHORT_LITERAL148=(Token)match(input,SHORT_LITERAL,FOLLOW_SHORT_LITERAL_in_fixed_literal2796);
					SHORT_LITERAL148_tree = (CommonTree)adaptor.create(SHORT_LITERAL148);
					adaptor.addChild(root_0, SHORT_LITERAL148_tree);

					}
					break;
				case 4 :
					// smaliParser.g:684:5: BYTE_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BYTE_LITERAL149=(Token)match(input,BYTE_LITERAL,FOLLOW_BYTE_LITERAL_in_fixed_literal2802);
					BYTE_LITERAL149_tree = (CommonTree)adaptor.create(BYTE_LITERAL149);
					adaptor.addChild(root_0, BYTE_LITERAL149_tree);

					}
					break;
				case 5 :
					// smaliParser.g:685:5: float_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_float_literal_in_fixed_literal2808);
					float_literal150=float_literal();
					state._fsp--;

					adaptor.addChild(root_0, float_literal150.getTree());

					}
					break;
				case 6 :
					// smaliParser.g:686:5: double_literal
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_double_literal_in_fixed_literal2814);
					double_literal151=double_literal();
					state._fsp--;

					adaptor.addChild(root_0, double_literal151.getTree());

					}
					break;
				case 7 :
					// smaliParser.g:687:5: CHAR_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					CHAR_LITERAL152=(Token)match(input,CHAR_LITERAL,FOLLOW_CHAR_LITERAL_in_fixed_literal2820);
					CHAR_LITERAL152_tree = (CommonTree)adaptor.create(CHAR_LITERAL152);
					adaptor.addChild(root_0, CHAR_LITERAL152_tree);

					}
					break;
				case 8 :
					// smaliParser.g:688:5: BOOL_LITERAL
					{
					root_0 = (CommonTree)adaptor.nil();


					BOOL_LITERAL153=(Token)match(input,BOOL_LITERAL,FOLLOW_BOOL_LITERAL_in_fixed_literal2826);
					BOOL_LITERAL153_tree = (CommonTree)adaptor.create(BOOL_LITERAL153);
					adaptor.addChild(root_0, BOOL_LITERAL153_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "fixed_literal"


	public static class array_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "array_literal"
	// smaliParser.g:690:1: array_literal : OPEN_BRACE ( literal ( COMMA literal )* |) CLOSE_BRACE -> ^( I_ENCODED_ARRAY[$start, \"I_ENCODED_ARRAY\"] ( literal )* ) ;
	public final smaliParser.array_literal_return array_literal() throws RecognitionException {
		smaliParser.array_literal_return retval = new smaliParser.array_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPEN_BRACE154=null;
		Token COMMA156=null;
		Token CLOSE_BRACE158=null;
		ParserRuleReturnScope literal155 =null;
		ParserRuleReturnScope literal157 =null;

		CommonTree OPEN_BRACE154_tree=null;
		CommonTree COMMA156_tree=null;
		CommonTree CLOSE_BRACE158_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleSubtreeStream stream_literal=new RewriteRuleSubtreeStream(adaptor,"rule literal");

		try {
			// smaliParser.g:691:3: ( OPEN_BRACE ( literal ( COMMA literal )* |) CLOSE_BRACE -> ^( I_ENCODED_ARRAY[$start, \"I_ENCODED_ARRAY\"] ( literal )* ) )
			// smaliParser.g:691:5: OPEN_BRACE ( literal ( COMMA literal )* |) CLOSE_BRACE
			{
			OPEN_BRACE154=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_array_literal2836);
			stream_OPEN_BRACE.add(OPEN_BRACE154);

			// smaliParser.g:691:16: ( literal ( COMMA literal )* |)
			int alt26=2;
			int LA26_0 = input.LA(1);
			if ( (LA26_0==ACCESS_SPEC||LA26_0==ANNOTATION_VISIBILITY||LA26_0==ARRAY_TYPE_PREFIX||(LA26_0 >= BOOL_LITERAL && LA26_0 <= BYTE_LITERAL)||(LA26_0 >= CHAR_LITERAL && LA26_0 <= CLASS_DESCRIPTOR)||(LA26_0 >= DOUBLE_LITERAL && LA26_0 <= DOUBLE_LITERAL_OR_ID)||LA26_0==ENUM_DIRECTIVE||(LA26_0 >= FLOAT_LITERAL && LA26_0 <= FLOAT_LITERAL_OR_ID)||(LA26_0 >= INSTRUCTION_FORMAT10t && LA26_0 <= INSTRUCTION_FORMAT10x_ODEX)||LA26_0==INSTRUCTION_FORMAT11x||LA26_0==INSTRUCTION_FORMAT12x_OR_ID||(LA26_0 >= INSTRUCTION_FORMAT21c_FIELD && LA26_0 <= INSTRUCTION_FORMAT21c_TYPE)||LA26_0==INSTRUCTION_FORMAT21t||(LA26_0 >= INSTRUCTION_FORMAT22c_FIELD && LA26_0 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA26_0 >= INSTRUCTION_FORMAT22s_OR_ID && LA26_0 <= INSTRUCTION_FORMAT22t)||LA26_0==INSTRUCTION_FORMAT23x||(LA26_0 >= INSTRUCTION_FORMAT31i_OR_ID && LA26_0 <= INSTRUCTION_FORMAT31t)||(LA26_0 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA26_0 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA26_0 >= INSTRUCTION_FORMAT45cc_METHOD && LA26_0 <= INSTRUCTION_FORMAT51l)||(LA26_0 >= LONG_LITERAL && LA26_0 <= MEMBER_NAME)||(LA26_0 >= METHOD_HANDLE_TYPE_FIELD && LA26_0 <= OPEN_PAREN)||(LA26_0 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA26_0 <= PRIMITIVE_TYPE)||LA26_0==REGISTER||(LA26_0 >= SHORT_LITERAL && LA26_0 <= SIMPLE_NAME)||(LA26_0 >= STRING_LITERAL && LA26_0 <= SUBANNOTATION_DIRECTIVE)||(LA26_0 >= VERIFICATION_ERROR_TYPE && LA26_0 <= VOID_TYPE)) ) {
				alt26=1;
			}
			else if ( (LA26_0==CLOSE_BRACE) ) {
				alt26=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 26, 0, input);
				throw nvae;
			}

			switch (alt26) {
				case 1 :
					// smaliParser.g:691:17: literal ( COMMA literal )*
					{
					pushFollow(FOLLOW_literal_in_array_literal2839);
					literal155=literal();
					state._fsp--;

					stream_literal.add(literal155.getTree());
					// smaliParser.g:691:25: ( COMMA literal )*
					loop25:
					while (true) {
						int alt25=2;
						int LA25_0 = input.LA(1);
						if ( (LA25_0==COMMA) ) {
							alt25=1;
						}

						switch (alt25) {
						case 1 :
							// smaliParser.g:691:26: COMMA literal
							{
							COMMA156=(Token)match(input,COMMA,FOLLOW_COMMA_in_array_literal2842);
							stream_COMMA.add(COMMA156);

							pushFollow(FOLLOW_literal_in_array_literal2844);
							literal157=literal();
							state._fsp--;

							stream_literal.add(literal157.getTree());
							}
							break;

						default :
							break loop25;
						}
					}

					}
					break;
				case 2 :
					// smaliParser.g:691:44:
					{
					}
					break;

			}

			CLOSE_BRACE158=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_array_literal2852);
			stream_CLOSE_BRACE.add(CLOSE_BRACE158);

			// AST REWRITE
			// elements: literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 692:5: -> ^( I_ENCODED_ARRAY[$start, \"I_ENCODED_ARRAY\"] ( literal )* )
			{
				// smaliParser.g:692:8: ^( I_ENCODED_ARRAY[$start, \"I_ENCODED_ARRAY\"] ( literal )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ENCODED_ARRAY, (retval.start), "I_ENCODED_ARRAY"), root_1);
				// smaliParser.g:692:53: ( literal )*
				while ( stream_literal.hasNext() ) {
					adaptor.addChild(root_1, stream_literal.nextTree());
				}
				stream_literal.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "array_literal"


	public static class annotation_element_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "annotation_element"
	// smaliParser.g:694:1: annotation_element : simple_name EQUAL literal -> ^( I_ANNOTATION_ELEMENT[$start, \"I_ANNOTATION_ELEMENT\"] simple_name literal ) ;
	public final smaliParser.annotation_element_return annotation_element() throws RecognitionException {
		smaliParser.annotation_element_return retval = new smaliParser.annotation_element_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EQUAL160=null;
		ParserRuleReturnScope simple_name159 =null;
		ParserRuleReturnScope literal161 =null;

		CommonTree EQUAL160_tree=null;
		RewriteRuleTokenStream stream_EQUAL=new RewriteRuleTokenStream(adaptor,"token EQUAL");
		RewriteRuleSubtreeStream stream_simple_name=new RewriteRuleSubtreeStream(adaptor,"rule simple_name");
		RewriteRuleSubtreeStream stream_literal=new RewriteRuleSubtreeStream(adaptor,"rule literal");

		try {
			// smaliParser.g:695:3: ( simple_name EQUAL literal -> ^( I_ANNOTATION_ELEMENT[$start, \"I_ANNOTATION_ELEMENT\"] simple_name literal ) )
			// smaliParser.g:695:5: simple_name EQUAL literal
			{
			pushFollow(FOLLOW_simple_name_in_annotation_element2876);
			simple_name159=simple_name();
			state._fsp--;

			stream_simple_name.add(simple_name159.getTree());
			EQUAL160=(Token)match(input,EQUAL,FOLLOW_EQUAL_in_annotation_element2878);
			stream_EQUAL.add(EQUAL160);

			pushFollow(FOLLOW_literal_in_annotation_element2880);
			literal161=literal();
			state._fsp--;

			stream_literal.add(literal161.getTree());
			// AST REWRITE
			// elements: simple_name, literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 696:5: -> ^( I_ANNOTATION_ELEMENT[$start, \"I_ANNOTATION_ELEMENT\"] simple_name literal )
			{
				// smaliParser.g:696:8: ^( I_ANNOTATION_ELEMENT[$start, \"I_ANNOTATION_ELEMENT\"] simple_name literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATION_ELEMENT, (retval.start), "I_ANNOTATION_ELEMENT"), root_1);
				adaptor.addChild(root_1, stream_simple_name.nextTree());
				adaptor.addChild(root_1, stream_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "annotation_element"


	public static class annotation_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "annotation"
	// smaliParser.g:698:1: annotation : ANNOTATION_DIRECTIVE ANNOTATION_VISIBILITY CLASS_DESCRIPTOR ( annotation_element )* END_ANNOTATION_DIRECTIVE -> ^( I_ANNOTATION[$start, \"I_ANNOTATION\"] ANNOTATION_VISIBILITY ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) ) ;
	public final smaliParser.annotation_return annotation() throws RecognitionException {
		smaliParser.annotation_return retval = new smaliParser.annotation_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ANNOTATION_DIRECTIVE162=null;
		Token ANNOTATION_VISIBILITY163=null;
		Token CLASS_DESCRIPTOR164=null;
		Token END_ANNOTATION_DIRECTIVE166=null;
		ParserRuleReturnScope annotation_element165 =null;

		CommonTree ANNOTATION_DIRECTIVE162_tree=null;
		CommonTree ANNOTATION_VISIBILITY163_tree=null;
		CommonTree CLASS_DESCRIPTOR164_tree=null;
		CommonTree END_ANNOTATION_DIRECTIVE166_tree=null;
		RewriteRuleTokenStream stream_ANNOTATION_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token ANNOTATION_DIRECTIVE");
		RewriteRuleTokenStream stream_ANNOTATION_VISIBILITY=new RewriteRuleTokenStream(adaptor,"token ANNOTATION_VISIBILITY");
		RewriteRuleTokenStream stream_CLASS_DESCRIPTOR=new RewriteRuleTokenStream(adaptor,"token CLASS_DESCRIPTOR");
		RewriteRuleTokenStream stream_END_ANNOTATION_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_ANNOTATION_DIRECTIVE");
		RewriteRuleSubtreeStream stream_annotation_element=new RewriteRuleSubtreeStream(adaptor,"rule annotation_element");

		try {
			// smaliParser.g:699:3: ( ANNOTATION_DIRECTIVE ANNOTATION_VISIBILITY CLASS_DESCRIPTOR ( annotation_element )* END_ANNOTATION_DIRECTIVE -> ^( I_ANNOTATION[$start, \"I_ANNOTATION\"] ANNOTATION_VISIBILITY ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) ) )
			// smaliParser.g:699:5: ANNOTATION_DIRECTIVE ANNOTATION_VISIBILITY CLASS_DESCRIPTOR ( annotation_element )* END_ANNOTATION_DIRECTIVE
			{
			ANNOTATION_DIRECTIVE162=(Token)match(input,ANNOTATION_DIRECTIVE,FOLLOW_ANNOTATION_DIRECTIVE_in_annotation2905);
			stream_ANNOTATION_DIRECTIVE.add(ANNOTATION_DIRECTIVE162);

			ANNOTATION_VISIBILITY163=(Token)match(input,ANNOTATION_VISIBILITY,FOLLOW_ANNOTATION_VISIBILITY_in_annotation2907);
			stream_ANNOTATION_VISIBILITY.add(ANNOTATION_VISIBILITY163);

			CLASS_DESCRIPTOR164=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_annotation2909);
			stream_CLASS_DESCRIPTOR.add(CLASS_DESCRIPTOR164);

			// smaliParser.g:700:5: ( annotation_element )*
			loop27:
			while (true) {
				int alt27=2;
				int LA27_0 = input.LA(1);
				if ( (LA27_0==ACCESS_SPEC||LA27_0==ANNOTATION_VISIBILITY||LA27_0==BOOL_LITERAL||LA27_0==DOUBLE_LITERAL_OR_ID||LA27_0==FLOAT_LITERAL_OR_ID||(LA27_0 >= INSTRUCTION_FORMAT10t && LA27_0 <= INSTRUCTION_FORMAT10x_ODEX)||LA27_0==INSTRUCTION_FORMAT11x||LA27_0==INSTRUCTION_FORMAT12x_OR_ID||(LA27_0 >= INSTRUCTION_FORMAT21c_FIELD && LA27_0 <= INSTRUCTION_FORMAT21c_TYPE)||LA27_0==INSTRUCTION_FORMAT21t||(LA27_0 >= INSTRUCTION_FORMAT22c_FIELD && LA27_0 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA27_0 >= INSTRUCTION_FORMAT22s_OR_ID && LA27_0 <= INSTRUCTION_FORMAT22t)||LA27_0==INSTRUCTION_FORMAT23x||(LA27_0 >= INSTRUCTION_FORMAT31i_OR_ID && LA27_0 <= INSTRUCTION_FORMAT31t)||(LA27_0 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA27_0 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA27_0 >= INSTRUCTION_FORMAT45cc_METHOD && LA27_0 <= INSTRUCTION_FORMAT51l)||(LA27_0 >= METHOD_HANDLE_TYPE_FIELD && LA27_0 <= NULL_LITERAL)||(LA27_0 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA27_0 <= PRIMITIVE_TYPE)||LA27_0==REGISTER||LA27_0==SIMPLE_NAME||(LA27_0 >= VERIFICATION_ERROR_TYPE && LA27_0 <= VOID_TYPE)) ) {
					alt27=1;
				}

				switch (alt27) {
				case 1 :
					// smaliParser.g:700:5: annotation_element
					{
					pushFollow(FOLLOW_annotation_element_in_annotation2915);
					annotation_element165=annotation_element();
					state._fsp--;

					stream_annotation_element.add(annotation_element165.getTree());
					}
					break;

				default :
					break loop27;
				}
			}

			END_ANNOTATION_DIRECTIVE166=(Token)match(input,END_ANNOTATION_DIRECTIVE,FOLLOW_END_ANNOTATION_DIRECTIVE_in_annotation2918);
			stream_END_ANNOTATION_DIRECTIVE.add(END_ANNOTATION_DIRECTIVE166);

			// AST REWRITE
			// elements: annotation_element, CLASS_DESCRIPTOR, ANNOTATION_VISIBILITY
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 701:5: -> ^( I_ANNOTATION[$start, \"I_ANNOTATION\"] ANNOTATION_VISIBILITY ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) )
			{
				// smaliParser.g:701:8: ^( I_ANNOTATION[$start, \"I_ANNOTATION\"] ANNOTATION_VISIBILITY ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATION, (retval.start), "I_ANNOTATION"), root_1);
				adaptor.addChild(root_1, stream_ANNOTATION_VISIBILITY.nextNode());
				// smaliParser.g:701:69: ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SUBANNOTATION, (retval.start), "I_SUBANNOTATION"), root_2);
				adaptor.addChild(root_2, stream_CLASS_DESCRIPTOR.nextNode());
				// smaliParser.g:701:131: ( annotation_element )*
				while ( stream_annotation_element.hasNext() ) {
					adaptor.addChild(root_2, stream_annotation_element.nextTree());
				}
				stream_annotation_element.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "annotation"


	public static class subannotation_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "subannotation"
	// smaliParser.g:703:1: subannotation : SUBANNOTATION_DIRECTIVE CLASS_DESCRIPTOR ( annotation_element )* END_SUBANNOTATION_DIRECTIVE -> ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) ;
	public final smaliParser.subannotation_return subannotation() throws RecognitionException {
		smaliParser.subannotation_return retval = new smaliParser.subannotation_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SUBANNOTATION_DIRECTIVE167=null;
		Token CLASS_DESCRIPTOR168=null;
		Token END_SUBANNOTATION_DIRECTIVE170=null;
		ParserRuleReturnScope annotation_element169 =null;

		CommonTree SUBANNOTATION_DIRECTIVE167_tree=null;
		CommonTree CLASS_DESCRIPTOR168_tree=null;
		CommonTree END_SUBANNOTATION_DIRECTIVE170_tree=null;
		RewriteRuleTokenStream stream_SUBANNOTATION_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token SUBANNOTATION_DIRECTIVE");
		RewriteRuleTokenStream stream_CLASS_DESCRIPTOR=new RewriteRuleTokenStream(adaptor,"token CLASS_DESCRIPTOR");
		RewriteRuleTokenStream stream_END_SUBANNOTATION_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_SUBANNOTATION_DIRECTIVE");
		RewriteRuleSubtreeStream stream_annotation_element=new RewriteRuleSubtreeStream(adaptor,"rule annotation_element");

		try {
			// smaliParser.g:704:3: ( SUBANNOTATION_DIRECTIVE CLASS_DESCRIPTOR ( annotation_element )* END_SUBANNOTATION_DIRECTIVE -> ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* ) )
			// smaliParser.g:704:5: SUBANNOTATION_DIRECTIVE CLASS_DESCRIPTOR ( annotation_element )* END_SUBANNOTATION_DIRECTIVE
			{
			SUBANNOTATION_DIRECTIVE167=(Token)match(input,SUBANNOTATION_DIRECTIVE,FOLLOW_SUBANNOTATION_DIRECTIVE_in_subannotation2951);
			stream_SUBANNOTATION_DIRECTIVE.add(SUBANNOTATION_DIRECTIVE167);

			CLASS_DESCRIPTOR168=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_subannotation2953);
			stream_CLASS_DESCRIPTOR.add(CLASS_DESCRIPTOR168);

			// smaliParser.g:704:46: ( annotation_element )*
			loop28:
			while (true) {
				int alt28=2;
				int LA28_0 = input.LA(1);
				if ( (LA28_0==ACCESS_SPEC||LA28_0==ANNOTATION_VISIBILITY||LA28_0==BOOL_LITERAL||LA28_0==DOUBLE_LITERAL_OR_ID||LA28_0==FLOAT_LITERAL_OR_ID||(LA28_0 >= INSTRUCTION_FORMAT10t && LA28_0 <= INSTRUCTION_FORMAT10x_ODEX)||LA28_0==INSTRUCTION_FORMAT11x||LA28_0==INSTRUCTION_FORMAT12x_OR_ID||(LA28_0 >= INSTRUCTION_FORMAT21c_FIELD && LA28_0 <= INSTRUCTION_FORMAT21c_TYPE)||LA28_0==INSTRUCTION_FORMAT21t||(LA28_0 >= INSTRUCTION_FORMAT22c_FIELD && LA28_0 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA28_0 >= INSTRUCTION_FORMAT22s_OR_ID && LA28_0 <= INSTRUCTION_FORMAT22t)||LA28_0==INSTRUCTION_FORMAT23x||(LA28_0 >= INSTRUCTION_FORMAT31i_OR_ID && LA28_0 <= INSTRUCTION_FORMAT31t)||(LA28_0 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA28_0 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA28_0 >= INSTRUCTION_FORMAT45cc_METHOD && LA28_0 <= INSTRUCTION_FORMAT51l)||(LA28_0 >= METHOD_HANDLE_TYPE_FIELD && LA28_0 <= NULL_LITERAL)||(LA28_0 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA28_0 <= PRIMITIVE_TYPE)||LA28_0==REGISTER||LA28_0==SIMPLE_NAME||(LA28_0 >= VERIFICATION_ERROR_TYPE && LA28_0 <= VOID_TYPE)) ) {
					alt28=1;
				}

				switch (alt28) {
				case 1 :
					// smaliParser.g:704:46: annotation_element
					{
					pushFollow(FOLLOW_annotation_element_in_subannotation2955);
					annotation_element169=annotation_element();
					state._fsp--;

					stream_annotation_element.add(annotation_element169.getTree());
					}
					break;

				default :
					break loop28;
				}
			}

			END_SUBANNOTATION_DIRECTIVE170=(Token)match(input,END_SUBANNOTATION_DIRECTIVE,FOLLOW_END_SUBANNOTATION_DIRECTIVE_in_subannotation2958);
			stream_END_SUBANNOTATION_DIRECTIVE.add(END_SUBANNOTATION_DIRECTIVE170);

			// AST REWRITE
			// elements: CLASS_DESCRIPTOR, annotation_element
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 705:5: -> ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* )
			{
				// smaliParser.g:705:8: ^( I_SUBANNOTATION[$start, \"I_SUBANNOTATION\"] CLASS_DESCRIPTOR ( annotation_element )* )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SUBANNOTATION, (retval.start), "I_SUBANNOTATION"), root_1);
				adaptor.addChild(root_1, stream_CLASS_DESCRIPTOR.nextNode());
				// smaliParser.g:705:70: ( annotation_element )*
				while ( stream_annotation_element.hasNext() ) {
					adaptor.addChild(root_1, stream_annotation_element.nextTree());
				}
				stream_annotation_element.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "subannotation"


	public static class enum_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "enum_literal"
	// smaliParser.g:708:1: enum_literal : ENUM_DIRECTIVE field_reference -> ^( I_ENCODED_ENUM field_reference ) ;
	public final smaliParser.enum_literal_return enum_literal() throws RecognitionException {
		smaliParser.enum_literal_return retval = new smaliParser.enum_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ENUM_DIRECTIVE171=null;
		ParserRuleReturnScope field_reference172 =null;

		CommonTree ENUM_DIRECTIVE171_tree=null;
		RewriteRuleTokenStream stream_ENUM_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token ENUM_DIRECTIVE");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:709:3: ( ENUM_DIRECTIVE field_reference -> ^( I_ENCODED_ENUM field_reference ) )
			// smaliParser.g:709:5: ENUM_DIRECTIVE field_reference
			{
			ENUM_DIRECTIVE171=(Token)match(input,ENUM_DIRECTIVE,FOLLOW_ENUM_DIRECTIVE_in_enum_literal2985);
			stream_ENUM_DIRECTIVE.add(ENUM_DIRECTIVE171);

			pushFollow(FOLLOW_field_reference_in_enum_literal2987);
			field_reference172=field_reference();
			state._fsp--;

			stream_field_reference.add(field_reference172.getTree());
			// AST REWRITE
			// elements: field_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 710:3: -> ^( I_ENCODED_ENUM field_reference )
			{
				// smaliParser.g:710:6: ^( I_ENCODED_ENUM field_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ENCODED_ENUM, "I_ENCODED_ENUM"), root_1);
				adaptor.addChild(root_1, stream_field_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "enum_literal"


	public static class type_field_method_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "type_field_method_literal"
	// smaliParser.g:712:1: type_field_method_literal : ( reference_type_descriptor | ( ( reference_type_descriptor ARROW )? ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) ) ) | PRIMITIVE_TYPE | VOID_TYPE );
	public final smaliParser.type_field_method_literal_return type_field_method_literal() throws RecognitionException {
		smaliParser.type_field_method_literal_return retval = new smaliParser.type_field_method_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ARROW175=null;
		Token COLON177=null;
		Token PRIMITIVE_TYPE181=null;
		Token VOID_TYPE182=null;
		ParserRuleReturnScope reference_type_descriptor173 =null;
		ParserRuleReturnScope reference_type_descriptor174 =null;
		ParserRuleReturnScope member_name176 =null;
		ParserRuleReturnScope nonvoid_type_descriptor178 =null;
		ParserRuleReturnScope member_name179 =null;
		ParserRuleReturnScope method_prototype180 =null;

		CommonTree ARROW175_tree=null;
		CommonTree COLON177_tree=null;
		CommonTree PRIMITIVE_TYPE181_tree=null;
		CommonTree VOID_TYPE182_tree=null;
		RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");
		RewriteRuleSubtreeStream stream_member_name=new RewriteRuleSubtreeStream(adaptor,"rule member_name");
		RewriteRuleSubtreeStream stream_reference_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule reference_type_descriptor");

		try {
			// smaliParser.g:713:3: ( reference_type_descriptor | ( ( reference_type_descriptor ARROW )? ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) ) ) | PRIMITIVE_TYPE | VOID_TYPE )
			int alt31=4;
			switch ( input.LA(1) ) {
			case CLASS_DESCRIPTOR:
				{
				int LA31_1 = input.LA(2);
				if ( (LA31_1==EOF||(LA31_1 >= ACCESS_SPEC && LA31_1 <= ANNOTATION_VISIBILITY)||LA31_1==BOOL_LITERAL||(LA31_1 >= CLASS_DIRECTIVE && LA31_1 <= CLOSE_PAREN)||LA31_1==COMMA||(LA31_1 >= DOUBLE_LITERAL_OR_ID && LA31_1 <= END_ANNOTATION_DIRECTIVE)||LA31_1==END_FIELD_DIRECTIVE||LA31_1==END_SUBANNOTATION_DIRECTIVE||LA31_1==FIELD_DIRECTIVE||(LA31_1 >= FLOAT_LITERAL_OR_ID && LA31_1 <= IMPLEMENTS_DIRECTIVE)||(LA31_1 >= INSTRUCTION_FORMAT10t && LA31_1 <= INSTRUCTION_FORMAT10x_ODEX)||LA31_1==INSTRUCTION_FORMAT11x||LA31_1==INSTRUCTION_FORMAT12x_OR_ID||(LA31_1 >= INSTRUCTION_FORMAT21c_FIELD && LA31_1 <= INSTRUCTION_FORMAT21c_TYPE)||LA31_1==INSTRUCTION_FORMAT21t||(LA31_1 >= INSTRUCTION_FORMAT22c_FIELD && LA31_1 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA31_1 >= INSTRUCTION_FORMAT22s_OR_ID && LA31_1 <= INSTRUCTION_FORMAT22t)||LA31_1==INSTRUCTION_FORMAT23x||(LA31_1 >= INSTRUCTION_FORMAT31i_OR_ID && LA31_1 <= INSTRUCTION_FORMAT31t)||(LA31_1 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA31_1 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA31_1 >= INSTRUCTION_FORMAT45cc_METHOD && LA31_1 <= INSTRUCTION_FORMAT51l)||(LA31_1 >= METHOD_DIRECTIVE && LA31_1 <= NULL_LITERAL)||(LA31_1 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA31_1 <= PRIMITIVE_TYPE)||LA31_1==REGISTER||(LA31_1 >= SIMPLE_NAME && LA31_1 <= SOURCE_DIRECTIVE)||(LA31_1 >= SUPER_DIRECTIVE && LA31_1 <= VOID_TYPE)) ) {
					alt31=1;
				}
				else if ( (LA31_1==ARROW) ) {
					alt31=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 31, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ARRAY_TYPE_PREFIX:
				{
				int LA31_2 = input.LA(2);
				if ( (LA31_2==CLASS_DESCRIPTOR||LA31_2==PRIMITIVE_TYPE) ) {
					int LA31_7 = input.LA(3);
					if ( (LA31_7==EOF||(LA31_7 >= ACCESS_SPEC && LA31_7 <= ANNOTATION_VISIBILITY)||LA31_7==BOOL_LITERAL||(LA31_7 >= CLASS_DIRECTIVE && LA31_7 <= CLOSE_PAREN)||LA31_7==COMMA||(LA31_7 >= DOUBLE_LITERAL_OR_ID && LA31_7 <= END_ANNOTATION_DIRECTIVE)||LA31_7==END_FIELD_DIRECTIVE||LA31_7==END_SUBANNOTATION_DIRECTIVE||LA31_7==FIELD_DIRECTIVE||(LA31_7 >= FLOAT_LITERAL_OR_ID && LA31_7 <= IMPLEMENTS_DIRECTIVE)||(LA31_7 >= INSTRUCTION_FORMAT10t && LA31_7 <= INSTRUCTION_FORMAT10x_ODEX)||LA31_7==INSTRUCTION_FORMAT11x||LA31_7==INSTRUCTION_FORMAT12x_OR_ID||(LA31_7 >= INSTRUCTION_FORMAT21c_FIELD && LA31_7 <= INSTRUCTION_FORMAT21c_TYPE)||LA31_7==INSTRUCTION_FORMAT21t||(LA31_7 >= INSTRUCTION_FORMAT22c_FIELD && LA31_7 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA31_7 >= INSTRUCTION_FORMAT22s_OR_ID && LA31_7 <= INSTRUCTION_FORMAT22t)||LA31_7==INSTRUCTION_FORMAT23x||(LA31_7 >= INSTRUCTION_FORMAT31i_OR_ID && LA31_7 <= INSTRUCTION_FORMAT31t)||(LA31_7 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA31_7 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA31_7 >= INSTRUCTION_FORMAT45cc_METHOD && LA31_7 <= INSTRUCTION_FORMAT51l)||(LA31_7 >= METHOD_DIRECTIVE && LA31_7 <= NULL_LITERAL)||(LA31_7 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA31_7 <= PRIMITIVE_TYPE)||LA31_7==REGISTER||(LA31_7 >= SIMPLE_NAME && LA31_7 <= SOURCE_DIRECTIVE)||(LA31_7 >= SUPER_DIRECTIVE && LA31_7 <= VOID_TYPE)) ) {
						alt31=1;
					}
					else if ( (LA31_7==ARROW) ) {
						alt31=2;
					}

					else {
						int nvaeMark = input.mark();
						try {
							for (int nvaeConsume = 0; nvaeConsume < 3 - 1; nvaeConsume++) {
								input.consume();
							}
							NoViableAltException nvae =
								new NoViableAltException("", 31, 7, input);
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
							new NoViableAltException("", 31, 2, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case ACCESS_SPEC:
			case ANNOTATION_VISIBILITY:
			case BOOL_LITERAL:
			case DOUBLE_LITERAL_OR_ID:
			case FLOAT_LITERAL_OR_ID:
			case INSTRUCTION_FORMAT10t:
			case INSTRUCTION_FORMAT10x:
			case INSTRUCTION_FORMAT10x_ODEX:
			case INSTRUCTION_FORMAT11x:
			case INSTRUCTION_FORMAT12x_OR_ID:
			case INSTRUCTION_FORMAT21c_FIELD:
			case INSTRUCTION_FORMAT21c_FIELD_ODEX:
			case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
			case INSTRUCTION_FORMAT21c_METHOD_TYPE:
			case INSTRUCTION_FORMAT21c_STRING:
			case INSTRUCTION_FORMAT21c_TYPE:
			case INSTRUCTION_FORMAT21t:
			case INSTRUCTION_FORMAT22c_FIELD:
			case INSTRUCTION_FORMAT22c_FIELD_ODEX:
			case INSTRUCTION_FORMAT22c_TYPE:
			case INSTRUCTION_FORMAT22cs_FIELD:
			case INSTRUCTION_FORMAT22s_OR_ID:
			case INSTRUCTION_FORMAT22t:
			case INSTRUCTION_FORMAT23x:
			case INSTRUCTION_FORMAT31i_OR_ID:
			case INSTRUCTION_FORMAT31t:
			case INSTRUCTION_FORMAT35c_CALL_SITE:
			case INSTRUCTION_FORMAT35c_METHOD:
			case INSTRUCTION_FORMAT35c_METHOD_ODEX:
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
			case INSTRUCTION_FORMAT35c_TYPE:
			case INSTRUCTION_FORMAT35mi_METHOD:
			case INSTRUCTION_FORMAT35ms_METHOD:
			case INSTRUCTION_FORMAT45cc_METHOD:
			case INSTRUCTION_FORMAT4rcc_METHOD:
			case INSTRUCTION_FORMAT51l:
			case MEMBER_NAME:
			case METHOD_HANDLE_TYPE_FIELD:
			case METHOD_HANDLE_TYPE_METHOD:
			case NEGATIVE_INTEGER_LITERAL:
			case NULL_LITERAL:
			case PARAM_LIST_OR_ID_PRIMITIVE_TYPE:
			case POSITIVE_INTEGER_LITERAL:
			case REGISTER:
			case SIMPLE_NAME:
			case VERIFICATION_ERROR_TYPE:
				{
				alt31=2;
				}
				break;
			case PRIMITIVE_TYPE:
				{
				int LA31_4 = input.LA(2);
				if ( (LA31_4==COLON||LA31_4==OPEN_PAREN) ) {
					alt31=2;
				}
				else if ( (LA31_4==EOF||(LA31_4 >= ACCESS_SPEC && LA31_4 <= ANNOTATION_VISIBILITY)||LA31_4==BOOL_LITERAL||(LA31_4 >= CLASS_DIRECTIVE && LA31_4 <= CLOSE_PAREN)||LA31_4==COMMA||(LA31_4 >= DOUBLE_LITERAL_OR_ID && LA31_4 <= END_ANNOTATION_DIRECTIVE)||LA31_4==END_FIELD_DIRECTIVE||LA31_4==END_SUBANNOTATION_DIRECTIVE||LA31_4==FIELD_DIRECTIVE||(LA31_4 >= FLOAT_LITERAL_OR_ID && LA31_4 <= IMPLEMENTS_DIRECTIVE)||(LA31_4 >= INSTRUCTION_FORMAT10t && LA31_4 <= INSTRUCTION_FORMAT10x_ODEX)||LA31_4==INSTRUCTION_FORMAT11x||LA31_4==INSTRUCTION_FORMAT12x_OR_ID||(LA31_4 >= INSTRUCTION_FORMAT21c_FIELD && LA31_4 <= INSTRUCTION_FORMAT21c_TYPE)||LA31_4==INSTRUCTION_FORMAT21t||(LA31_4 >= INSTRUCTION_FORMAT22c_FIELD && LA31_4 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA31_4 >= INSTRUCTION_FORMAT22s_OR_ID && LA31_4 <= INSTRUCTION_FORMAT22t)||LA31_4==INSTRUCTION_FORMAT23x||(LA31_4 >= INSTRUCTION_FORMAT31i_OR_ID && LA31_4 <= INSTRUCTION_FORMAT31t)||(LA31_4 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA31_4 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA31_4 >= INSTRUCTION_FORMAT45cc_METHOD && LA31_4 <= INSTRUCTION_FORMAT51l)||(LA31_4 >= METHOD_DIRECTIVE && LA31_4 <= NULL_LITERAL)||(LA31_4 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA31_4 <= PRIMITIVE_TYPE)||LA31_4==REGISTER||(LA31_4 >= SIMPLE_NAME && LA31_4 <= SOURCE_DIRECTIVE)||(LA31_4 >= SUPER_DIRECTIVE && LA31_4 <= VOID_TYPE)) ) {
					alt31=3;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 31, 4, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			case VOID_TYPE:
				{
				int LA31_5 = input.LA(2);
				if ( (LA31_5==COLON||LA31_5==OPEN_PAREN) ) {
					alt31=2;
				}
				else if ( (LA31_5==EOF||(LA31_5 >= ACCESS_SPEC && LA31_5 <= ANNOTATION_VISIBILITY)||LA31_5==BOOL_LITERAL||(LA31_5 >= CLASS_DIRECTIVE && LA31_5 <= CLOSE_PAREN)||LA31_5==COMMA||(LA31_5 >= DOUBLE_LITERAL_OR_ID && LA31_5 <= END_ANNOTATION_DIRECTIVE)||LA31_5==END_FIELD_DIRECTIVE||LA31_5==END_SUBANNOTATION_DIRECTIVE||LA31_5==FIELD_DIRECTIVE||(LA31_5 >= FLOAT_LITERAL_OR_ID && LA31_5 <= IMPLEMENTS_DIRECTIVE)||(LA31_5 >= INSTRUCTION_FORMAT10t && LA31_5 <= INSTRUCTION_FORMAT10x_ODEX)||LA31_5==INSTRUCTION_FORMAT11x||LA31_5==INSTRUCTION_FORMAT12x_OR_ID||(LA31_5 >= INSTRUCTION_FORMAT21c_FIELD && LA31_5 <= INSTRUCTION_FORMAT21c_TYPE)||LA31_5==INSTRUCTION_FORMAT21t||(LA31_5 >= INSTRUCTION_FORMAT22c_FIELD && LA31_5 <= INSTRUCTION_FORMAT22cs_FIELD)||(LA31_5 >= INSTRUCTION_FORMAT22s_OR_ID && LA31_5 <= INSTRUCTION_FORMAT22t)||LA31_5==INSTRUCTION_FORMAT23x||(LA31_5 >= INSTRUCTION_FORMAT31i_OR_ID && LA31_5 <= INSTRUCTION_FORMAT31t)||(LA31_5 >= INSTRUCTION_FORMAT35c_CALL_SITE && LA31_5 <= INSTRUCTION_FORMAT35ms_METHOD)||(LA31_5 >= INSTRUCTION_FORMAT45cc_METHOD && LA31_5 <= INSTRUCTION_FORMAT51l)||(LA31_5 >= METHOD_DIRECTIVE && LA31_5 <= NULL_LITERAL)||(LA31_5 >= PARAM_LIST_OR_ID_PRIMITIVE_TYPE && LA31_5 <= PRIMITIVE_TYPE)||LA31_5==REGISTER||(LA31_5 >= SIMPLE_NAME && LA31_5 <= SOURCE_DIRECTIVE)||(LA31_5 >= SUPER_DIRECTIVE && LA31_5 <= VOID_TYPE)) ) {
					alt31=4;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 31, 5, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 31, 0, input);
				throw nvae;
			}
			switch (alt31) {
				case 1 :
					// smaliParser.g:713:5: reference_type_descriptor
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_reference_type_descriptor_in_type_field_method_literal3007);
					reference_type_descriptor173=reference_type_descriptor();
					state._fsp--;

					adaptor.addChild(root_0, reference_type_descriptor173.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:714:5: ( ( reference_type_descriptor ARROW )? ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) ) )
					{
					// smaliParser.g:714:5: ( ( reference_type_descriptor ARROW )? ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) ) )
					// smaliParser.g:714:7: ( reference_type_descriptor ARROW )? ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) )
					{
					// smaliParser.g:714:7: ( reference_type_descriptor ARROW )?
					int alt29=2;
					int LA29_0 = input.LA(1);
					if ( (LA29_0==ARRAY_TYPE_PREFIX||LA29_0==CLASS_DESCRIPTOR) ) {
						alt29=1;
					}
					switch (alt29) {
						case 1 :
							// smaliParser.g:714:8: reference_type_descriptor ARROW
							{
							pushFollow(FOLLOW_reference_type_descriptor_in_type_field_method_literal3016);
							reference_type_descriptor174=reference_type_descriptor();
							state._fsp--;

							stream_reference_type_descriptor.add(reference_type_descriptor174.getTree());
							ARROW175=(Token)match(input,ARROW,FOLLOW_ARROW_in_type_field_method_literal3018);
							stream_ARROW.add(ARROW175);

							}
							break;

					}

					// smaliParser.g:715:7: ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) )
					int alt30=2;
					alt30 = dfa30.predict(input);
					switch (alt30) {
						case 1 :
							// smaliParser.g:715:9: member_name COLON nonvoid_type_descriptor
							{
							pushFollow(FOLLOW_member_name_in_type_field_method_literal3030);
							member_name176=member_name();
							state._fsp--;

							stream_member_name.add(member_name176.getTree());
							COLON177=(Token)match(input,COLON,FOLLOW_COLON_in_type_field_method_literal3032);
							stream_COLON.add(COLON177);

							pushFollow(FOLLOW_nonvoid_type_descriptor_in_type_field_method_literal3034);
							nonvoid_type_descriptor178=nonvoid_type_descriptor();
							state._fsp--;

							stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor178.getTree());
							// AST REWRITE
							// elements: member_name, reference_type_descriptor, nonvoid_type_descriptor
							// token labels:
							// rule labels: retval
							// token list labels:
							// rule list labels:
							// wildcard labels:
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 715:51: -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor )
							{
								// smaliParser.g:715:54: ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ENCODED_FIELD, "I_ENCODED_FIELD"), root_1);
								// smaliParser.g:715:72: ( reference_type_descriptor )?
								if ( stream_reference_type_descriptor.hasNext() ) {
									adaptor.addChild(root_1, stream_reference_type_descriptor.nextTree());
								}
								stream_reference_type_descriptor.reset();

								adaptor.addChild(root_1, stream_member_name.nextTree());
								adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;
						case 2 :
							// smaliParser.g:716:9: member_name method_prototype
							{
							pushFollow(FOLLOW_member_name_in_type_field_method_literal3057);
							member_name179=member_name();
							state._fsp--;

							stream_member_name.add(member_name179.getTree());
							pushFollow(FOLLOW_method_prototype_in_type_field_method_literal3059);
							method_prototype180=method_prototype();
							state._fsp--;

							stream_method_prototype.add(method_prototype180.getTree());
							// AST REWRITE
							// elements: method_prototype, reference_type_descriptor, member_name
							// token labels:
							// rule labels: retval
							// token list labels:
							// rule list labels:
							// wildcard labels:
							retval.tree = root_0;
							RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

							root_0 = (CommonTree)adaptor.nil();
							// 716:38: -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype )
							{
								// smaliParser.g:716:41: ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype )
								{
								CommonTree root_1 = (CommonTree)adaptor.nil();
								root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ENCODED_METHOD, "I_ENCODED_METHOD"), root_1);
								// smaliParser.g:716:60: ( reference_type_descriptor )?
								if ( stream_reference_type_descriptor.hasNext() ) {
									adaptor.addChild(root_1, stream_reference_type_descriptor.nextTree());
								}
								stream_reference_type_descriptor.reset();

								adaptor.addChild(root_1, stream_member_name.nextTree());
								adaptor.addChild(root_1, stream_method_prototype.nextTree());
								adaptor.addChild(root_0, root_1);
								}

							}


							retval.tree = root_0;

							}
							break;

					}

					}

					}
					break;
				case 3 :
					// smaliParser.g:719:5: PRIMITIVE_TYPE
					{
					root_0 = (CommonTree)adaptor.nil();


					PRIMITIVE_TYPE181=(Token)match(input,PRIMITIVE_TYPE,FOLLOW_PRIMITIVE_TYPE_in_type_field_method_literal3092);
					PRIMITIVE_TYPE181_tree = (CommonTree)adaptor.create(PRIMITIVE_TYPE181);
					adaptor.addChild(root_0, PRIMITIVE_TYPE181_tree);

					}
					break;
				case 4 :
					// smaliParser.g:720:5: VOID_TYPE
					{
					root_0 = (CommonTree)adaptor.nil();


					VOID_TYPE182=(Token)match(input,VOID_TYPE,FOLLOW_VOID_TYPE_in_type_field_method_literal3098);
					VOID_TYPE182_tree = (CommonTree)adaptor.create(VOID_TYPE182);
					adaptor.addChild(root_0, VOID_TYPE182_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "type_field_method_literal"


	public static class call_site_reference_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "call_site_reference"
	// smaliParser.g:722:1: call_site_reference : simple_name OPEN_PAREN STRING_LITERAL COMMA method_prototype ( COMMA literal )* CLOSE_PAREN AT method_reference -> ^( I_CALL_SITE_REFERENCE simple_name STRING_LITERAL method_prototype ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) method_reference ) ;
	public final smaliParser.call_site_reference_return call_site_reference() throws RecognitionException {
		smaliParser.call_site_reference_return retval = new smaliParser.call_site_reference_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPEN_PAREN184=null;
		Token STRING_LITERAL185=null;
		Token COMMA186=null;
		Token COMMA188=null;
		Token CLOSE_PAREN190=null;
		Token AT191=null;
		ParserRuleReturnScope simple_name183 =null;
		ParserRuleReturnScope method_prototype187 =null;
		ParserRuleReturnScope literal189 =null;
		ParserRuleReturnScope method_reference192 =null;

		CommonTree OPEN_PAREN184_tree=null;
		CommonTree STRING_LITERAL185_tree=null;
		CommonTree COMMA186_tree=null;
		CommonTree COMMA188_tree=null;
		CommonTree CLOSE_PAREN190_tree=null;
		CommonTree AT191_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_PAREN=new RewriteRuleTokenStream(adaptor,"token OPEN_PAREN");
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_CLOSE_PAREN=new RewriteRuleTokenStream(adaptor,"token CLOSE_PAREN");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_simple_name=new RewriteRuleSubtreeStream(adaptor,"rule simple_name");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_literal=new RewriteRuleSubtreeStream(adaptor,"rule literal");

		try {
			// smaliParser.g:723:3: ( simple_name OPEN_PAREN STRING_LITERAL COMMA method_prototype ( COMMA literal )* CLOSE_PAREN AT method_reference -> ^( I_CALL_SITE_REFERENCE simple_name STRING_LITERAL method_prototype ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) method_reference ) )
			// smaliParser.g:723:5: simple_name OPEN_PAREN STRING_LITERAL COMMA method_prototype ( COMMA literal )* CLOSE_PAREN AT method_reference
			{
			pushFollow(FOLLOW_simple_name_in_call_site_reference3108);
			simple_name183=simple_name();
			state._fsp--;

			stream_simple_name.add(simple_name183.getTree());
			OPEN_PAREN184=(Token)match(input,OPEN_PAREN,FOLLOW_OPEN_PAREN_in_call_site_reference3110);
			stream_OPEN_PAREN.add(OPEN_PAREN184);

			STRING_LITERAL185=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_call_site_reference3112);
			stream_STRING_LITERAL.add(STRING_LITERAL185);

			COMMA186=(Token)match(input,COMMA,FOLLOW_COMMA_in_call_site_reference3114);
			stream_COMMA.add(COMMA186);

			pushFollow(FOLLOW_method_prototype_in_call_site_reference3116);
			method_prototype187=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype187.getTree());
			// smaliParser.g:723:66: ( COMMA literal )*
			loop32:
			while (true) {
				int alt32=2;
				int LA32_0 = input.LA(1);
				if ( (LA32_0==COMMA) ) {
					alt32=1;
				}

				switch (alt32) {
				case 1 :
					// smaliParser.g:723:67: COMMA literal
					{
					COMMA188=(Token)match(input,COMMA,FOLLOW_COMMA_in_call_site_reference3119);
					stream_COMMA.add(COMMA188);

					pushFollow(FOLLOW_literal_in_call_site_reference3121);
					literal189=literal();
					state._fsp--;

					stream_literal.add(literal189.getTree());
					}
					break;

				default :
					break loop32;
				}
			}

			CLOSE_PAREN190=(Token)match(input,CLOSE_PAREN,FOLLOW_CLOSE_PAREN_in_call_site_reference3125);
			stream_CLOSE_PAREN.add(CLOSE_PAREN190);

			AT191=(Token)match(input,AT,FOLLOW_AT_in_call_site_reference3127);
			stream_AT.add(AT191);

			pushFollow(FOLLOW_method_reference_in_call_site_reference3129);
			method_reference192=method_reference();
			state._fsp--;

			stream_method_reference.add(method_reference192.getTree());
			// AST REWRITE
			// elements: method_reference, simple_name, literal, STRING_LITERAL, method_prototype
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 724:5: -> ^( I_CALL_SITE_REFERENCE simple_name STRING_LITERAL method_prototype ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) method_reference )
			{
				// smaliParser.g:724:8: ^( I_CALL_SITE_REFERENCE simple_name STRING_LITERAL method_prototype ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* ) method_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CALL_SITE_REFERENCE, "I_CALL_SITE_REFERENCE"), root_1);
				adaptor.addChild(root_1, stream_simple_name.nextTree());
				adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
				adaptor.addChild(root_1, stream_method_prototype.nextTree());
				// smaliParser.g:724:76: ^( I_CALL_SITE_EXTRA_ARGUMENTS ( literal )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CALL_SITE_EXTRA_ARGUMENTS, "I_CALL_SITE_EXTRA_ARGUMENTS"), root_2);
				// smaliParser.g:724:106: ( literal )*
				while ( stream_literal.hasNext() ) {
					adaptor.addChild(root_2, stream_literal.nextTree());
				}
				stream_literal.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_1, stream_method_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "call_site_reference"


	public static class method_handle_reference_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "method_handle_reference"
	// smaliParser.g:727:1: method_handle_reference : ( METHOD_HANDLE_TYPE_FIELD AT field_reference -> METHOD_HANDLE_TYPE_FIELD field_reference | METHOD_HANDLE_TYPE_METHOD AT method_reference -> METHOD_HANDLE_TYPE_METHOD method_reference | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE AT method_reference -> INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE method_reference );
	public final smaliParser.method_handle_reference_return method_handle_reference() throws RecognitionException {
		smaliParser.method_handle_reference_return retval = new smaliParser.method_handle_reference_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token METHOD_HANDLE_TYPE_FIELD193=null;
		Token AT194=null;
		Token METHOD_HANDLE_TYPE_METHOD196=null;
		Token AT197=null;
		Token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE199=null;
		Token AT200=null;
		ParserRuleReturnScope field_reference195 =null;
		ParserRuleReturnScope method_reference198 =null;
		ParserRuleReturnScope method_reference201 =null;

		CommonTree METHOD_HANDLE_TYPE_FIELD193_tree=null;
		CommonTree AT194_tree=null;
		CommonTree METHOD_HANDLE_TYPE_METHOD196_tree=null;
		CommonTree AT197_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE199_tree=null;
		CommonTree AT200_tree=null;
		RewriteRuleTokenStream stream_AT=new RewriteRuleTokenStream(adaptor,"token AT");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE");
		RewriteRuleTokenStream stream_METHOD_HANDLE_TYPE_FIELD=new RewriteRuleTokenStream(adaptor,"token METHOD_HANDLE_TYPE_FIELD");
		RewriteRuleTokenStream stream_METHOD_HANDLE_TYPE_METHOD=new RewriteRuleTokenStream(adaptor,"token METHOD_HANDLE_TYPE_METHOD");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:728:3: ( METHOD_HANDLE_TYPE_FIELD AT field_reference -> METHOD_HANDLE_TYPE_FIELD field_reference | METHOD_HANDLE_TYPE_METHOD AT method_reference -> METHOD_HANDLE_TYPE_METHOD method_reference | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE AT method_reference -> INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE method_reference )
			int alt33=3;
			switch ( input.LA(1) ) {
			case METHOD_HANDLE_TYPE_FIELD:
				{
				alt33=1;
				}
				break;
			case METHOD_HANDLE_TYPE_METHOD:
				{
				alt33=2;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
				{
				alt33=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 33, 0, input);
				throw nvae;
			}
			switch (alt33) {
				case 1 :
					// smaliParser.g:728:5: METHOD_HANDLE_TYPE_FIELD AT field_reference
					{
					METHOD_HANDLE_TYPE_FIELD193=(Token)match(input,METHOD_HANDLE_TYPE_FIELD,FOLLOW_METHOD_HANDLE_TYPE_FIELD_in_method_handle_reference3173);
					stream_METHOD_HANDLE_TYPE_FIELD.add(METHOD_HANDLE_TYPE_FIELD193);

					AT194=(Token)match(input,AT,FOLLOW_AT_in_method_handle_reference3175);
					stream_AT.add(AT194);

					pushFollow(FOLLOW_field_reference_in_method_handle_reference3177);
					field_reference195=field_reference();
					state._fsp--;

					stream_field_reference.add(field_reference195.getTree());
					// AST REWRITE
					// elements: METHOD_HANDLE_TYPE_FIELD, field_reference
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 728:49: -> METHOD_HANDLE_TYPE_FIELD field_reference
					{
						adaptor.addChild(root_0, stream_METHOD_HANDLE_TYPE_FIELD.nextNode());
						adaptor.addChild(root_0, stream_field_reference.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:729:5: METHOD_HANDLE_TYPE_METHOD AT method_reference
					{
					METHOD_HANDLE_TYPE_METHOD196=(Token)match(input,METHOD_HANDLE_TYPE_METHOD,FOLLOW_METHOD_HANDLE_TYPE_METHOD_in_method_handle_reference3189);
					stream_METHOD_HANDLE_TYPE_METHOD.add(METHOD_HANDLE_TYPE_METHOD196);

					AT197=(Token)match(input,AT,FOLLOW_AT_in_method_handle_reference3191);
					stream_AT.add(AT197);

					pushFollow(FOLLOW_method_reference_in_method_handle_reference3193);
					method_reference198=method_reference();
					state._fsp--;

					stream_method_reference.add(method_reference198.getTree());
					// AST REWRITE
					// elements: METHOD_HANDLE_TYPE_METHOD, method_reference
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 729:51: -> METHOD_HANDLE_TYPE_METHOD method_reference
					{
						adaptor.addChild(root_0, stream_METHOD_HANDLE_TYPE_METHOD.nextNode());
						adaptor.addChild(root_0, stream_method_reference.nextTree());
					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// smaliParser.g:730:5: INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE AT method_reference
					{
					INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE199=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_method_handle_reference3205);
					stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE.add(INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE199);

					AT200=(Token)match(input,AT,FOLLOW_AT_in_method_handle_reference3207);
					stream_AT.add(AT200);

					pushFollow(FOLLOW_method_reference_in_method_handle_reference3209);
					method_reference201=method_reference();
					state._fsp--;

					stream_method_reference.add(method_reference201.getTree());
					// AST REWRITE
					// elements: INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE, method_reference
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 730:76: -> INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE method_reference
					{
						adaptor.addChild(root_0, stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE.nextNode());
						adaptor.addChild(root_0, stream_method_reference.nextTree());
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_handle_reference"


	public static class method_handle_literal_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "method_handle_literal"
	// smaliParser.g:732:1: method_handle_literal : method_handle_reference -> ^( I_ENCODED_METHOD_HANDLE method_handle_reference ) ;
	public final smaliParser.method_handle_literal_return method_handle_literal() throws RecognitionException {
		smaliParser.method_handle_literal_return retval = new smaliParser.method_handle_literal_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope method_handle_reference202 =null;

		RewriteRuleSubtreeStream stream_method_handle_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_handle_reference");

		try {
			// smaliParser.g:733:3: ( method_handle_reference -> ^( I_ENCODED_METHOD_HANDLE method_handle_reference ) )
			// smaliParser.g:733:5: method_handle_reference
			{
			pushFollow(FOLLOW_method_handle_reference_in_method_handle_literal3225);
			method_handle_reference202=method_handle_reference();
			state._fsp--;

			stream_method_handle_reference.add(method_handle_reference202.getTree());
			// AST REWRITE
			// elements: method_handle_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 734:3: -> ^( I_ENCODED_METHOD_HANDLE method_handle_reference )
			{
				// smaliParser.g:734:6: ^( I_ENCODED_METHOD_HANDLE method_handle_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ENCODED_METHOD_HANDLE, "I_ENCODED_METHOD_HANDLE"), root_1);
				adaptor.addChild(root_1, stream_method_handle_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_handle_literal"


	public static class method_reference_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "method_reference"
	// smaliParser.g:736:1: method_reference : ( reference_type_descriptor ARROW )? member_name method_prototype -> ( reference_type_descriptor )? member_name method_prototype ;
	public final smaliParser.method_reference_return method_reference() throws RecognitionException {
		smaliParser.method_reference_return retval = new smaliParser.method_reference_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ARROW204=null;
		ParserRuleReturnScope reference_type_descriptor203 =null;
		ParserRuleReturnScope member_name205 =null;
		ParserRuleReturnScope method_prototype206 =null;

		CommonTree ARROW204_tree=null;
		RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_member_name=new RewriteRuleSubtreeStream(adaptor,"rule member_name");
		RewriteRuleSubtreeStream stream_reference_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule reference_type_descriptor");

		try {
			// smaliParser.g:737:3: ( ( reference_type_descriptor ARROW )? member_name method_prototype -> ( reference_type_descriptor )? member_name method_prototype )
			// smaliParser.g:737:5: ( reference_type_descriptor ARROW )? member_name method_prototype
			{
			// smaliParser.g:737:5: ( reference_type_descriptor ARROW )?
			int alt34=2;
			int LA34_0 = input.LA(1);
			if ( (LA34_0==ARRAY_TYPE_PREFIX||LA34_0==CLASS_DESCRIPTOR) ) {
				alt34=1;
			}
			switch (alt34) {
				case 1 :
					// smaliParser.g:737:6: reference_type_descriptor ARROW
					{
					pushFollow(FOLLOW_reference_type_descriptor_in_method_reference3246);
					reference_type_descriptor203=reference_type_descriptor();
					state._fsp--;

					stream_reference_type_descriptor.add(reference_type_descriptor203.getTree());
					ARROW204=(Token)match(input,ARROW,FOLLOW_ARROW_in_method_reference3248);
					stream_ARROW.add(ARROW204);

					}
					break;

			}

			pushFollow(FOLLOW_member_name_in_method_reference3252);
			member_name205=member_name();
			state._fsp--;

			stream_member_name.add(member_name205.getTree());
			pushFollow(FOLLOW_method_prototype_in_method_reference3254);
			method_prototype206=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype206.getTree());
			// AST REWRITE
			// elements: reference_type_descriptor, method_prototype, member_name
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 738:3: -> ( reference_type_descriptor )? member_name method_prototype
			{
				// smaliParser.g:738:6: ( reference_type_descriptor )?
				if ( stream_reference_type_descriptor.hasNext() ) {
					adaptor.addChild(root_0, stream_reference_type_descriptor.nextTree());
				}
				stream_reference_type_descriptor.reset();

				adaptor.addChild(root_0, stream_member_name.nextTree());
				adaptor.addChild(root_0, stream_method_prototype.nextTree());
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "method_reference"


	public static class field_reference_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "field_reference"
	// smaliParser.g:740:1: field_reference : ( reference_type_descriptor ARROW )? member_name COLON nonvoid_type_descriptor -> ( reference_type_descriptor )? member_name nonvoid_type_descriptor ;
	public final smaliParser.field_reference_return field_reference() throws RecognitionException {
		smaliParser.field_reference_return retval = new smaliParser.field_reference_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ARROW208=null;
		Token COLON210=null;
		ParserRuleReturnScope reference_type_descriptor207 =null;
		ParserRuleReturnScope member_name209 =null;
		ParserRuleReturnScope nonvoid_type_descriptor211 =null;

		CommonTree ARROW208_tree=null;
		CommonTree COLON210_tree=null;
		RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");
		RewriteRuleSubtreeStream stream_member_name=new RewriteRuleSubtreeStream(adaptor,"rule member_name");
		RewriteRuleSubtreeStream stream_reference_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule reference_type_descriptor");

		try {
			// smaliParser.g:741:3: ( ( reference_type_descriptor ARROW )? member_name COLON nonvoid_type_descriptor -> ( reference_type_descriptor )? member_name nonvoid_type_descriptor )
			// smaliParser.g:741:5: ( reference_type_descriptor ARROW )? member_name COLON nonvoid_type_descriptor
			{
			// smaliParser.g:741:5: ( reference_type_descriptor ARROW )?
			int alt35=2;
			int LA35_0 = input.LA(1);
			if ( (LA35_0==ARRAY_TYPE_PREFIX||LA35_0==CLASS_DESCRIPTOR) ) {
				alt35=1;
			}
			switch (alt35) {
				case 1 :
					// smaliParser.g:741:6: reference_type_descriptor ARROW
					{
					pushFollow(FOLLOW_reference_type_descriptor_in_field_reference3276);
					reference_type_descriptor207=reference_type_descriptor();
					state._fsp--;

					stream_reference_type_descriptor.add(reference_type_descriptor207.getTree());
					ARROW208=(Token)match(input,ARROW,FOLLOW_ARROW_in_field_reference3278);
					stream_ARROW.add(ARROW208);

					}
					break;

			}

			pushFollow(FOLLOW_member_name_in_field_reference3282);
			member_name209=member_name();
			state._fsp--;

			stream_member_name.add(member_name209.getTree());
			COLON210=(Token)match(input,COLON,FOLLOW_COLON_in_field_reference3284);
			stream_COLON.add(COLON210);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_field_reference3286);
			nonvoid_type_descriptor211=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor211.getTree());
			// AST REWRITE
			// elements: member_name, nonvoid_type_descriptor, reference_type_descriptor
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 742:3: -> ( reference_type_descriptor )? member_name nonvoid_type_descriptor
			{
				// smaliParser.g:742:6: ( reference_type_descriptor )?
				if ( stream_reference_type_descriptor.hasNext() ) {
					adaptor.addChild(root_0, stream_reference_type_descriptor.nextTree());
				}
				stream_reference_type_descriptor.reset();

				adaptor.addChild(root_0, stream_member_name.nextTree());
				adaptor.addChild(root_0, stream_nonvoid_type_descriptor.nextTree());
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "field_reference"


	public static class label_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "label"
	// smaliParser.g:744:1: label : COLON simple_name -> ^( I_LABEL[$COLON, \"I_LABEL\"] simple_name ) ;
	public final smaliParser.label_return label() throws RecognitionException {
		smaliParser.label_return retval = new smaliParser.label_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON212=null;
		ParserRuleReturnScope simple_name213 =null;

		CommonTree COLON212_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_simple_name=new RewriteRuleSubtreeStream(adaptor,"rule simple_name");

		try {
			// smaliParser.g:745:3: ( COLON simple_name -> ^( I_LABEL[$COLON, \"I_LABEL\"] simple_name ) )
			// smaliParser.g:745:5: COLON simple_name
			{
			COLON212=(Token)match(input,COLON,FOLLOW_COLON_in_label3307);
			stream_COLON.add(COLON212);

			pushFollow(FOLLOW_simple_name_in_label3309);
			simple_name213=simple_name();
			state._fsp--;

			stream_simple_name.add(simple_name213.getTree());
			// AST REWRITE
			// elements: simple_name
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 745:23: -> ^( I_LABEL[$COLON, \"I_LABEL\"] simple_name )
			{
				// smaliParser.g:745:26: ^( I_LABEL[$COLON, \"I_LABEL\"] simple_name )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_LABEL, COLON212, "I_LABEL"), root_1);
				adaptor.addChild(root_1, stream_simple_name.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "label"


	public static class label_ref_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "label_ref"
	// smaliParser.g:747:1: label_ref : COLON simple_name -> simple_name ;
	public final smaliParser.label_ref_return label_ref() throws RecognitionException {
		smaliParser.label_ref_return retval = new smaliParser.label_ref_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token COLON214=null;
		ParserRuleReturnScope simple_name215 =null;

		CommonTree COLON214_tree=null;
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleSubtreeStream stream_simple_name=new RewriteRuleSubtreeStream(adaptor,"rule simple_name");

		try {
			// smaliParser.g:748:3: ( COLON simple_name -> simple_name )
			// smaliParser.g:748:5: COLON simple_name
			{
			COLON214=(Token)match(input,COLON,FOLLOW_COLON_in_label_ref3328);
			stream_COLON.add(COLON214);

			pushFollow(FOLLOW_simple_name_in_label_ref3330);
			simple_name215=simple_name();
			state._fsp--;

			stream_simple_name.add(simple_name215.getTree());
			// AST REWRITE
			// elements: simple_name
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 748:23: -> simple_name
			{
				adaptor.addChild(root_0, stream_simple_name.nextTree());
			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "label_ref"


	public static class register_list_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "register_list"
	// smaliParser.g:750:1: register_list : ( REGISTER ( COMMA REGISTER )* -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ( REGISTER )* ) | -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ) );
	public final smaliParser.register_list_return register_list() throws RecognitionException {
		smaliParser.register_list_return retval = new smaliParser.register_list_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token REGISTER216=null;
		Token COMMA217=null;
		Token REGISTER218=null;

		CommonTree REGISTER216_tree=null;
		CommonTree COMMA217_tree=null;
		CommonTree REGISTER218_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");

		try {
			// smaliParser.g:751:3: ( REGISTER ( COMMA REGISTER )* -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ( REGISTER )* ) | -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ) )
			int alt37=2;
			int LA37_0 = input.LA(1);
			if ( (LA37_0==REGISTER) ) {
				alt37=1;
			}
			else if ( (LA37_0==CLOSE_BRACE) ) {
				alt37=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 37, 0, input);
				throw nvae;
			}

			switch (alt37) {
				case 1 :
					// smaliParser.g:751:5: REGISTER ( COMMA REGISTER )*
					{
					REGISTER216=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_register_list3344);
					stream_REGISTER.add(REGISTER216);

					// smaliParser.g:751:14: ( COMMA REGISTER )*
					loop36:
					while (true) {
						int alt36=2;
						int LA36_0 = input.LA(1);
						if ( (LA36_0==COMMA) ) {
							alt36=1;
						}

						switch (alt36) {
						case 1 :
							// smaliParser.g:751:15: COMMA REGISTER
							{
							COMMA217=(Token)match(input,COMMA,FOLLOW_COMMA_in_register_list3347);
							stream_COMMA.add(COMMA217);

							REGISTER218=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_register_list3349);
							stream_REGISTER.add(REGISTER218);

							}
							break;

						default :
							break loop36;
						}
					}

					// AST REWRITE
					// elements: REGISTER
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 751:32: -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ( REGISTER )* )
					{
						// smaliParser.g:751:35: ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] ( REGISTER )* )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_REGISTER_LIST, (retval.start), "I_REGISTER_LIST"), root_1);
						// smaliParser.g:751:80: ( REGISTER )*
						while ( stream_REGISTER.hasNext() ) {
							adaptor.addChild(root_1, stream_REGISTER.nextNode());
						}
						stream_REGISTER.reset();

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:752:5:
					{
					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 752:5: -> ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] )
					{
						// smaliParser.g:752:7: ^( I_REGISTER_LIST[$start, \"I_REGISTER_LIST\"] )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_REGISTER_LIST, (retval.start), "I_REGISTER_LIST"), root_1);
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "register_list"


	public static class register_range_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "register_range"
	// smaliParser.g:754:1: register_range : (startreg= REGISTER ( DOTDOT endreg= REGISTER )? )? -> ^( I_REGISTER_RANGE[$start, \"I_REGISTER_RANGE\"] ( $startreg)? ( $endreg)? ) ;
	public final smaliParser.register_range_return register_range() throws RecognitionException {
		smaliParser.register_range_return retval = new smaliParser.register_range_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token startreg=null;
		Token endreg=null;
		Token DOTDOT219=null;

		CommonTree startreg_tree=null;
		CommonTree endreg_tree=null;
		CommonTree DOTDOT219_tree=null;
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");

		try {
			// smaliParser.g:755:3: ( (startreg= REGISTER ( DOTDOT endreg= REGISTER )? )? -> ^( I_REGISTER_RANGE[$start, \"I_REGISTER_RANGE\"] ( $startreg)? ( $endreg)? ) )
			// smaliParser.g:755:5: (startreg= REGISTER ( DOTDOT endreg= REGISTER )? )?
			{
			// smaliParser.g:755:5: (startreg= REGISTER ( DOTDOT endreg= REGISTER )? )?
			int alt39=2;
			int LA39_0 = input.LA(1);
			if ( (LA39_0==REGISTER) ) {
				alt39=1;
			}
			switch (alt39) {
				case 1 :
					// smaliParser.g:755:6: startreg= REGISTER ( DOTDOT endreg= REGISTER )?
					{
					startreg=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_register_range3384);
					stream_REGISTER.add(startreg);

					// smaliParser.g:755:24: ( DOTDOT endreg= REGISTER )?
					int alt38=2;
					int LA38_0 = input.LA(1);
					if ( (LA38_0==DOTDOT) ) {
						alt38=1;
					}
					switch (alt38) {
						case 1 :
							// smaliParser.g:755:25: DOTDOT endreg= REGISTER
							{
							DOTDOT219=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_register_range3387);
							stream_DOTDOT.add(DOTDOT219);

							endreg=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_register_range3391);
							stream_REGISTER.add(endreg);

							}
							break;

					}

					}
					break;

			}

			// AST REWRITE
			// elements: endreg, startreg
			// token labels: endreg, startreg
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleTokenStream stream_endreg=new RewriteRuleTokenStream(adaptor,"token endreg",endreg);
			RewriteRuleTokenStream stream_startreg=new RewriteRuleTokenStream(adaptor,"token startreg",startreg);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 755:52: -> ^( I_REGISTER_RANGE[$start, \"I_REGISTER_RANGE\"] ( $startreg)? ( $endreg)? )
			{
				// smaliParser.g:755:55: ^( I_REGISTER_RANGE[$start, \"I_REGISTER_RANGE\"] ( $startreg)? ( $endreg)? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_REGISTER_RANGE, (retval.start), "I_REGISTER_RANGE"), root_1);
				// smaliParser.g:755:103: ( $startreg)?
				if ( stream_startreg.hasNext() ) {
					adaptor.addChild(root_1, stream_startreg.nextNode());
				}
				stream_startreg.reset();

				// smaliParser.g:755:114: ( $endreg)?
				if ( stream_endreg.hasNext() ) {
					adaptor.addChild(root_1, stream_endreg.nextNode());
				}
				stream_endreg.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "register_range"


	public static class verification_error_reference_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "verification_error_reference"
	// smaliParser.g:757:1: verification_error_reference : ( CLASS_DESCRIPTOR | field_reference | method_reference );
	public final smaliParser.verification_error_reference_return verification_error_reference() throws RecognitionException {
		smaliParser.verification_error_reference_return retval = new smaliParser.verification_error_reference_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CLASS_DESCRIPTOR220=null;
		ParserRuleReturnScope field_reference221 =null;
		ParserRuleReturnScope method_reference222 =null;

		CommonTree CLASS_DESCRIPTOR220_tree=null;

		try {
			// smaliParser.g:758:3: ( CLASS_DESCRIPTOR | field_reference | method_reference )
			int alt40=3;
			alt40 = dfa40.predict(input);
			switch (alt40) {
				case 1 :
					// smaliParser.g:758:5: CLASS_DESCRIPTOR
					{
					root_0 = (CommonTree)adaptor.nil();


					CLASS_DESCRIPTOR220=(Token)match(input,CLASS_DESCRIPTOR,FOLLOW_CLASS_DESCRIPTOR_in_verification_error_reference3420);
					CLASS_DESCRIPTOR220_tree = (CommonTree)adaptor.create(CLASS_DESCRIPTOR220);
					adaptor.addChild(root_0, CLASS_DESCRIPTOR220_tree);

					}
					break;
				case 2 :
					// smaliParser.g:758:24: field_reference
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_field_reference_in_verification_error_reference3424);
					field_reference221=field_reference();
					state._fsp--;

					adaptor.addChild(root_0, field_reference221.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:758:42: method_reference
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_method_reference_in_verification_error_reference3428);
					method_reference222=method_reference();
					state._fsp--;

					adaptor.addChild(root_0, method_reference222.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "verification_error_reference"


	public static class catch_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "catch_directive"
	// smaliParser.g:760:1: catch_directive : CATCH_DIRECTIVE nonvoid_type_descriptor OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref -> ^( I_CATCH[$start, \"I_CATCH\"] nonvoid_type_descriptor $from $to $using) ;
	public final smaliParser.catch_directive_return catch_directive() throws RecognitionException {
		smaliParser.catch_directive_return retval = new smaliParser.catch_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CATCH_DIRECTIVE223=null;
		Token OPEN_BRACE225=null;
		Token DOTDOT226=null;
		Token CLOSE_BRACE227=null;
		ParserRuleReturnScope from =null;
		ParserRuleReturnScope to =null;
		ParserRuleReturnScope using =null;
		ParserRuleReturnScope nonvoid_type_descriptor224 =null;

		CommonTree CATCH_DIRECTIVE223_tree=null;
		CommonTree OPEN_BRACE225_tree=null;
		CommonTree DOTDOT226_tree=null;
		CommonTree CLOSE_BRACE227_tree=null;
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
		RewriteRuleTokenStream stream_CATCH_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token CATCH_DIRECTIVE");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");

		try {
			// smaliParser.g:761:3: ( CATCH_DIRECTIVE nonvoid_type_descriptor OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref -> ^( I_CATCH[$start, \"I_CATCH\"] nonvoid_type_descriptor $from $to $using) )
			// smaliParser.g:761:5: CATCH_DIRECTIVE nonvoid_type_descriptor OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref
			{
			CATCH_DIRECTIVE223=(Token)match(input,CATCH_DIRECTIVE,FOLLOW_CATCH_DIRECTIVE_in_catch_directive3438);
			stream_CATCH_DIRECTIVE.add(CATCH_DIRECTIVE223);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_catch_directive3440);
			nonvoid_type_descriptor224=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor224.getTree());
			OPEN_BRACE225=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_catch_directive3442);
			stream_OPEN_BRACE.add(OPEN_BRACE225);

			pushFollow(FOLLOW_label_ref_in_catch_directive3446);
			from=label_ref();
			state._fsp--;

			stream_label_ref.add(from.getTree());
			DOTDOT226=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_catch_directive3448);
			stream_DOTDOT.add(DOTDOT226);

			pushFollow(FOLLOW_label_ref_in_catch_directive3452);
			to=label_ref();
			state._fsp--;

			stream_label_ref.add(to.getTree());
			CLOSE_BRACE227=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_catch_directive3454);
			stream_CLOSE_BRACE.add(CLOSE_BRACE227);

			pushFollow(FOLLOW_label_ref_in_catch_directive3458);
			using=label_ref();
			state._fsp--;

			stream_label_ref.add(using.getTree());
			// AST REWRITE
			// elements: nonvoid_type_descriptor, from, to, using
			// token labels:
			// rule labels: using, from, to, retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_using=new RewriteRuleSubtreeStream(adaptor,"rule using",using!=null?using.getTree():null);
			RewriteRuleSubtreeStream stream_from=new RewriteRuleSubtreeStream(adaptor,"rule from",from!=null?from.getTree():null);
			RewriteRuleSubtreeStream stream_to=new RewriteRuleSubtreeStream(adaptor,"rule to",to!=null?to.getTree():null);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 762:5: -> ^( I_CATCH[$start, \"I_CATCH\"] nonvoid_type_descriptor $from $to $using)
			{
				// smaliParser.g:762:8: ^( I_CATCH[$start, \"I_CATCH\"] nonvoid_type_descriptor $from $to $using)
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CATCH, (retval.start), "I_CATCH"), root_1);
				adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				adaptor.addChild(root_1, stream_from.nextTree());
				adaptor.addChild(root_1, stream_to.nextTree());
				adaptor.addChild(root_1, stream_using.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "catch_directive"


	public static class catchall_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "catchall_directive"
	// smaliParser.g:764:1: catchall_directive : CATCHALL_DIRECTIVE OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref -> ^( I_CATCHALL[$start, \"I_CATCHALL\"] $from $to $using) ;
	public final smaliParser.catchall_directive_return catchall_directive() throws RecognitionException {
		smaliParser.catchall_directive_return retval = new smaliParser.catchall_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token CATCHALL_DIRECTIVE228=null;
		Token OPEN_BRACE229=null;
		Token DOTDOT230=null;
		Token CLOSE_BRACE231=null;
		ParserRuleReturnScope from =null;
		ParserRuleReturnScope to =null;
		ParserRuleReturnScope using =null;

		CommonTree CATCHALL_DIRECTIVE228_tree=null;
		CommonTree OPEN_BRACE229_tree=null;
		CommonTree DOTDOT230_tree=null;
		CommonTree CLOSE_BRACE231_tree=null;
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_DOTDOT=new RewriteRuleTokenStream(adaptor,"token DOTDOT");
		RewriteRuleTokenStream stream_CATCHALL_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token CATCHALL_DIRECTIVE");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:765:3: ( CATCHALL_DIRECTIVE OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref -> ^( I_CATCHALL[$start, \"I_CATCHALL\"] $from $to $using) )
			// smaliParser.g:765:5: CATCHALL_DIRECTIVE OPEN_BRACE from= label_ref DOTDOT to= label_ref CLOSE_BRACE using= label_ref
			{
			CATCHALL_DIRECTIVE228=(Token)match(input,CATCHALL_DIRECTIVE,FOLLOW_CATCHALL_DIRECTIVE_in_catchall_directive3490);
			stream_CATCHALL_DIRECTIVE.add(CATCHALL_DIRECTIVE228);

			OPEN_BRACE229=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_catchall_directive3492);
			stream_OPEN_BRACE.add(OPEN_BRACE229);

			pushFollow(FOLLOW_label_ref_in_catchall_directive3496);
			from=label_ref();
			state._fsp--;

			stream_label_ref.add(from.getTree());
			DOTDOT230=(Token)match(input,DOTDOT,FOLLOW_DOTDOT_in_catchall_directive3498);
			stream_DOTDOT.add(DOTDOT230);

			pushFollow(FOLLOW_label_ref_in_catchall_directive3502);
			to=label_ref();
			state._fsp--;

			stream_label_ref.add(to.getTree());
			CLOSE_BRACE231=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_catchall_directive3504);
			stream_CLOSE_BRACE.add(CLOSE_BRACE231);

			pushFollow(FOLLOW_label_ref_in_catchall_directive3508);
			using=label_ref();
			state._fsp--;

			stream_label_ref.add(using.getTree());
			// AST REWRITE
			// elements: to, using, from
			// token labels:
			// rule labels: using, from, to, retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_using=new RewriteRuleSubtreeStream(adaptor,"rule using",using!=null?using.getTree():null);
			RewriteRuleSubtreeStream stream_from=new RewriteRuleSubtreeStream(adaptor,"rule from",from!=null?from.getTree():null);
			RewriteRuleSubtreeStream stream_to=new RewriteRuleSubtreeStream(adaptor,"rule to",to!=null?to.getTree():null);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 766:5: -> ^( I_CATCHALL[$start, \"I_CATCHALL\"] $from $to $using)
			{
				// smaliParser.g:766:8: ^( I_CATCHALL[$start, \"I_CATCHALL\"] $from $to $using)
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_CATCHALL, (retval.start), "I_CATCHALL"), root_1);
				adaptor.addChild(root_1, stream_from.nextTree());
				adaptor.addChild(root_1, stream_to.nextTree());
				adaptor.addChild(root_1, stream_using.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "catchall_directive"


	public static class parameter_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "parameter_directive"
	// smaliParser.g:772:1: parameter_directive : PARAMETER_DIRECTIVE REGISTER ( COMMA STRING_LITERAL )? ({...}? annotation )* ( END_PARAMETER_DIRECTIVE -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) ) ) ;
	public final smaliParser.parameter_directive_return parameter_directive() throws RecognitionException {
		smaliParser.parameter_directive_return retval = new smaliParser.parameter_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PARAMETER_DIRECTIVE232=null;
		Token REGISTER233=null;
		Token COMMA234=null;
		Token STRING_LITERAL235=null;
		Token END_PARAMETER_DIRECTIVE237=null;
		ParserRuleReturnScope annotation236 =null;

		CommonTree PARAMETER_DIRECTIVE232_tree=null;
		CommonTree REGISTER233_tree=null;
		CommonTree COMMA234_tree=null;
		CommonTree STRING_LITERAL235_tree=null;
		CommonTree END_PARAMETER_DIRECTIVE237_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_PARAMETER_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token PARAMETER_DIRECTIVE");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_END_PARAMETER_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_PARAMETER_DIRECTIVE");
		RewriteRuleSubtreeStream stream_annotation=new RewriteRuleSubtreeStream(adaptor,"rule annotation");

		List<CommonTree> annotations = new ArrayList<CommonTree>();
		try {
			// smaliParser.g:774:3: ( PARAMETER_DIRECTIVE REGISTER ( COMMA STRING_LITERAL )? ({...}? annotation )* ( END_PARAMETER_DIRECTIVE -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) ) ) )
			// smaliParser.g:774:5: PARAMETER_DIRECTIVE REGISTER ( COMMA STRING_LITERAL )? ({...}? annotation )* ( END_PARAMETER_DIRECTIVE -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) ) )
			{
			PARAMETER_DIRECTIVE232=(Token)match(input,PARAMETER_DIRECTIVE,FOLLOW_PARAMETER_DIRECTIVE_in_parameter_directive3547);
			stream_PARAMETER_DIRECTIVE.add(PARAMETER_DIRECTIVE232);

			REGISTER233=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_parameter_directive3549);
			stream_REGISTER.add(REGISTER233);

			// smaliParser.g:774:34: ( COMMA STRING_LITERAL )?
			int alt41=2;
			int LA41_0 = input.LA(1);
			if ( (LA41_0==COMMA) ) {
				alt41=1;
			}
			switch (alt41) {
				case 1 :
					// smaliParser.g:774:35: COMMA STRING_LITERAL
					{
					COMMA234=(Token)match(input,COMMA,FOLLOW_COMMA_in_parameter_directive3552);
					stream_COMMA.add(COMMA234);

					STRING_LITERAL235=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_parameter_directive3554);
					stream_STRING_LITERAL.add(STRING_LITERAL235);

					}
					break;

			}

			// smaliParser.g:775:5: ({...}? annotation )*
			loop42:
			while (true) {
				int alt42=2;
				alt42 = dfa42.predict(input);
				switch (alt42) {
				case 1 :
					// smaliParser.g:775:6: {...}? annotation
					{
					if ( !((input.LA(1) == ANNOTATION_DIRECTIVE)) ) {
						throw new FailedPredicateException(input, "parameter_directive", "input.LA(1) == ANNOTATION_DIRECTIVE");
					}
					pushFollow(FOLLOW_annotation_in_parameter_directive3565);
					annotation236=annotation();
					state._fsp--;

					stream_annotation.add(annotation236.getTree());
					annotations.add((annotation236!=null?((CommonTree)annotation236.getTree()):null));
					}
					break;

				default :
					break loop42;
				}
			}

			// smaliParser.g:777:5: ( END_PARAMETER_DIRECTIVE -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) ) | -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) ) )
			int alt43=2;
			int LA43_0 = input.LA(1);
			if ( (LA43_0==END_PARAMETER_DIRECTIVE) ) {
				alt43=1;
			}
			else if ( (LA43_0==ANNOTATION_DIRECTIVE||LA43_0==ARRAY_DATA_DIRECTIVE||(LA43_0 >= CATCHALL_DIRECTIVE && LA43_0 <= CATCH_DIRECTIVE)||LA43_0==COLON||(LA43_0 >= END_LOCAL_DIRECTIVE && LA43_0 <= END_METHOD_DIRECTIVE)||LA43_0==EPILOGUE_DIRECTIVE||(LA43_0 >= INSTRUCTION_FORMAT10t && LA43_0 <= INSTRUCTION_FORMAT51l)||(LA43_0 >= LINE_DIRECTIVE && LA43_0 <= LOCAL_DIRECTIVE)||(LA43_0 >= PACKED_SWITCH_DIRECTIVE && LA43_0 <= PARAMETER_DIRECTIVE)||LA43_0==PROLOGUE_DIRECTIVE||(LA43_0 >= REGISTERS_DIRECTIVE && LA43_0 <= RESTART_LOCAL_DIRECTIVE)||(LA43_0 >= SOURCE_DIRECTIVE && LA43_0 <= SPARSE_SWITCH_DIRECTIVE)) ) {
				alt43=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 43, 0, input);
				throw nvae;
			}

			switch (alt43) {
				case 1 :
					// smaliParser.g:777:7: END_PARAMETER_DIRECTIVE
					{
					END_PARAMETER_DIRECTIVE237=(Token)match(input,END_PARAMETER_DIRECTIVE,FOLLOW_END_PARAMETER_DIRECTIVE_in_parameter_directive3578);
					stream_END_PARAMETER_DIRECTIVE.add(END_PARAMETER_DIRECTIVE237);

					// AST REWRITE
					// elements: annotation, REGISTER, STRING_LITERAL
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 778:7: -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) )
					{
						// smaliParser.g:778:10: ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ( annotation )* ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PARAMETER, (retval.start), "I_PARAMETER"), root_1);
						adaptor.addChild(root_1, stream_REGISTER.nextNode());
						// smaliParser.g:778:56: ( STRING_LITERAL )?
						if ( stream_STRING_LITERAL.hasNext() ) {
							adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
						}
						stream_STRING_LITERAL.reset();

						// smaliParser.g:778:72: ^( I_ANNOTATIONS ( annotation )* )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATIONS, "I_ANNOTATIONS"), root_2);
						// smaliParser.g:778:88: ( annotation )*
						while ( stream_annotation.hasNext() ) {
							adaptor.addChild(root_2, stream_annotation.nextTree());
						}
						stream_annotation.reset();

						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// smaliParser.g:779:19:
					{
					statements_and_directives_stack.peek().methodAnnotations.addAll(annotations);
					// AST REWRITE
					// elements: REGISTER, STRING_LITERAL
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 780:7: -> ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) )
					{
						// smaliParser.g:780:10: ^( I_PARAMETER[$start, \"I_PARAMETER\"] REGISTER ( STRING_LITERAL )? ^( I_ANNOTATIONS ) )
						{
						CommonTree root_1 = (CommonTree)adaptor.nil();
						root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PARAMETER, (retval.start), "I_PARAMETER"), root_1);
						adaptor.addChild(root_1, stream_REGISTER.nextNode());
						// smaliParser.g:780:56: ( STRING_LITERAL )?
						if ( stream_STRING_LITERAL.hasNext() ) {
							adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
						}
						stream_STRING_LITERAL.reset();

						// smaliParser.g:780:72: ^( I_ANNOTATIONS )
						{
						CommonTree root_2 = (CommonTree)adaptor.nil();
						root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ANNOTATIONS, "I_ANNOTATIONS"), root_2);
						adaptor.addChild(root_1, root_2);
						}

						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "parameter_directive"


	public static class debug_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "debug_directive"
	// smaliParser.g:783:1: debug_directive : ( line_directive | local_directive | end_local_directive | restart_local_directive | prologue_directive | epilogue_directive | source_directive );
	public final smaliParser.debug_directive_return debug_directive() throws RecognitionException {
		smaliParser.debug_directive_return retval = new smaliParser.debug_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope line_directive238 =null;
		ParserRuleReturnScope local_directive239 =null;
		ParserRuleReturnScope end_local_directive240 =null;
		ParserRuleReturnScope restart_local_directive241 =null;
		ParserRuleReturnScope prologue_directive242 =null;
		ParserRuleReturnScope epilogue_directive243 =null;
		ParserRuleReturnScope source_directive244 =null;


		try {
			// smaliParser.g:784:3: ( line_directive | local_directive | end_local_directive | restart_local_directive | prologue_directive | epilogue_directive | source_directive )
			int alt44=7;
			switch ( input.LA(1) ) {
			case LINE_DIRECTIVE:
				{
				alt44=1;
				}
				break;
			case LOCAL_DIRECTIVE:
				{
				alt44=2;
				}
				break;
			case END_LOCAL_DIRECTIVE:
				{
				alt44=3;
				}
				break;
			case RESTART_LOCAL_DIRECTIVE:
				{
				alt44=4;
				}
				break;
			case PROLOGUE_DIRECTIVE:
				{
				alt44=5;
				}
				break;
			case EPILOGUE_DIRECTIVE:
				{
				alt44=6;
				}
				break;
			case SOURCE_DIRECTIVE:
				{
				alt44=7;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 44, 0, input);
				throw nvae;
			}
			switch (alt44) {
				case 1 :
					// smaliParser.g:784:5: line_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_line_directive_in_debug_directive3651);
					line_directive238=line_directive();
					state._fsp--;

					adaptor.addChild(root_0, line_directive238.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:785:5: local_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_local_directive_in_debug_directive3657);
					local_directive239=local_directive();
					state._fsp--;

					adaptor.addChild(root_0, local_directive239.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:786:5: end_local_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_end_local_directive_in_debug_directive3663);
					end_local_directive240=end_local_directive();
					state._fsp--;

					adaptor.addChild(root_0, end_local_directive240.getTree());

					}
					break;
				case 4 :
					// smaliParser.g:787:5: restart_local_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_restart_local_directive_in_debug_directive3669);
					restart_local_directive241=restart_local_directive();
					state._fsp--;

					adaptor.addChild(root_0, restart_local_directive241.getTree());

					}
					break;
				case 5 :
					// smaliParser.g:788:5: prologue_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_prologue_directive_in_debug_directive3675);
					prologue_directive242=prologue_directive();
					state._fsp--;

					adaptor.addChild(root_0, prologue_directive242.getTree());

					}
					break;
				case 6 :
					// smaliParser.g:789:5: epilogue_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_epilogue_directive_in_debug_directive3681);
					epilogue_directive243=epilogue_directive();
					state._fsp--;

					adaptor.addChild(root_0, epilogue_directive243.getTree());

					}
					break;
				case 7 :
					// smaliParser.g:790:5: source_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_source_directive_in_debug_directive3687);
					source_directive244=source_directive();
					state._fsp--;

					adaptor.addChild(root_0, source_directive244.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "debug_directive"


	public static class line_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "line_directive"
	// smaliParser.g:792:1: line_directive : LINE_DIRECTIVE integral_literal -> ^( I_LINE[$start, \"I_LINE\"] integral_literal ) ;
	public final smaliParser.line_directive_return line_directive() throws RecognitionException {
		smaliParser.line_directive_return retval = new smaliParser.line_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token LINE_DIRECTIVE245=null;
		ParserRuleReturnScope integral_literal246 =null;

		CommonTree LINE_DIRECTIVE245_tree=null;
		RewriteRuleTokenStream stream_LINE_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token LINE_DIRECTIVE");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:793:3: ( LINE_DIRECTIVE integral_literal -> ^( I_LINE[$start, \"I_LINE\"] integral_literal ) )
			// smaliParser.g:793:5: LINE_DIRECTIVE integral_literal
			{
			LINE_DIRECTIVE245=(Token)match(input,LINE_DIRECTIVE,FOLLOW_LINE_DIRECTIVE_in_line_directive3697);
			stream_LINE_DIRECTIVE.add(LINE_DIRECTIVE245);

			pushFollow(FOLLOW_integral_literal_in_line_directive3699);
			integral_literal246=integral_literal();
			state._fsp--;

			stream_integral_literal.add(integral_literal246.getTree());
			// AST REWRITE
			// elements: integral_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 794:5: -> ^( I_LINE[$start, \"I_LINE\"] integral_literal )
			{
				// smaliParser.g:794:8: ^( I_LINE[$start, \"I_LINE\"] integral_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_LINE, (retval.start), "I_LINE"), root_1);
				adaptor.addChild(root_1, stream_integral_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "line_directive"


	public static class local_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "local_directive"
	// smaliParser.g:796:1: local_directive : LOCAL_DIRECTIVE REGISTER ( COMMA ( NULL_LITERAL |name= STRING_LITERAL ) COLON ( VOID_TYPE | nonvoid_type_descriptor ) ( COMMA signature= STRING_LITERAL )? )? -> ^( I_LOCAL[$start, \"I_LOCAL\"] REGISTER ( NULL_LITERAL )? ( $name)? ( nonvoid_type_descriptor )? ( $signature)? ) ;
	public final smaliParser.local_directive_return local_directive() throws RecognitionException {
		smaliParser.local_directive_return retval = new smaliParser.local_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token name=null;
		Token signature=null;
		Token LOCAL_DIRECTIVE247=null;
		Token REGISTER248=null;
		Token COMMA249=null;
		Token NULL_LITERAL250=null;
		Token COLON251=null;
		Token VOID_TYPE252=null;
		Token COMMA254=null;
		ParserRuleReturnScope nonvoid_type_descriptor253 =null;

		CommonTree name_tree=null;
		CommonTree signature_tree=null;
		CommonTree LOCAL_DIRECTIVE247_tree=null;
		CommonTree REGISTER248_tree=null;
		CommonTree COMMA249_tree=null;
		CommonTree NULL_LITERAL250_tree=null;
		CommonTree COLON251_tree=null;
		CommonTree VOID_TYPE252_tree=null;
		CommonTree COMMA254_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_LOCAL_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token LOCAL_DIRECTIVE");
		RewriteRuleTokenStream stream_VOID_TYPE=new RewriteRuleTokenStream(adaptor,"token VOID_TYPE");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_COLON=new RewriteRuleTokenStream(adaptor,"token COLON");
		RewriteRuleTokenStream stream_NULL_LITERAL=new RewriteRuleTokenStream(adaptor,"token NULL_LITERAL");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");

		try {
			// smaliParser.g:797:3: ( LOCAL_DIRECTIVE REGISTER ( COMMA ( NULL_LITERAL |name= STRING_LITERAL ) COLON ( VOID_TYPE | nonvoid_type_descriptor ) ( COMMA signature= STRING_LITERAL )? )? -> ^( I_LOCAL[$start, \"I_LOCAL\"] REGISTER ( NULL_LITERAL )? ( $name)? ( nonvoid_type_descriptor )? ( $signature)? ) )
			// smaliParser.g:797:5: LOCAL_DIRECTIVE REGISTER ( COMMA ( NULL_LITERAL |name= STRING_LITERAL ) COLON ( VOID_TYPE | nonvoid_type_descriptor ) ( COMMA signature= STRING_LITERAL )? )?
			{
			LOCAL_DIRECTIVE247=(Token)match(input,LOCAL_DIRECTIVE,FOLLOW_LOCAL_DIRECTIVE_in_local_directive3722);
			stream_LOCAL_DIRECTIVE.add(LOCAL_DIRECTIVE247);

			REGISTER248=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_local_directive3724);
			stream_REGISTER.add(REGISTER248);

			// smaliParser.g:797:30: ( COMMA ( NULL_LITERAL |name= STRING_LITERAL ) COLON ( VOID_TYPE | nonvoid_type_descriptor ) ( COMMA signature= STRING_LITERAL )? )?
			int alt48=2;
			int LA48_0 = input.LA(1);
			if ( (LA48_0==COMMA) ) {
				alt48=1;
			}
			switch (alt48) {
				case 1 :
					// smaliParser.g:797:31: COMMA ( NULL_LITERAL |name= STRING_LITERAL ) COLON ( VOID_TYPE | nonvoid_type_descriptor ) ( COMMA signature= STRING_LITERAL )?
					{
					COMMA249=(Token)match(input,COMMA,FOLLOW_COMMA_in_local_directive3727);
					stream_COMMA.add(COMMA249);

					// smaliParser.g:797:37: ( NULL_LITERAL |name= STRING_LITERAL )
					int alt45=2;
					int LA45_0 = input.LA(1);
					if ( (LA45_0==NULL_LITERAL) ) {
						alt45=1;
					}
					else if ( (LA45_0==STRING_LITERAL) ) {
						alt45=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 45, 0, input);
						throw nvae;
					}

					switch (alt45) {
						case 1 :
							// smaliParser.g:797:38: NULL_LITERAL
							{
							NULL_LITERAL250=(Token)match(input,NULL_LITERAL,FOLLOW_NULL_LITERAL_in_local_directive3730);
							stream_NULL_LITERAL.add(NULL_LITERAL250);

							}
							break;
						case 2 :
							// smaliParser.g:797:53: name= STRING_LITERAL
							{
							name=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_local_directive3736);
							stream_STRING_LITERAL.add(name);

							}
							break;

					}

					COLON251=(Token)match(input,COLON,FOLLOW_COLON_in_local_directive3739);
					stream_COLON.add(COLON251);

					// smaliParser.g:797:80: ( VOID_TYPE | nonvoid_type_descriptor )
					int alt46=2;
					int LA46_0 = input.LA(1);
					if ( (LA46_0==VOID_TYPE) ) {
						alt46=1;
					}
					else if ( (LA46_0==ARRAY_TYPE_PREFIX||LA46_0==CLASS_DESCRIPTOR||LA46_0==PRIMITIVE_TYPE) ) {
						alt46=2;
					}

					else {
						NoViableAltException nvae =
							new NoViableAltException("", 46, 0, input);
						throw nvae;
					}

					switch (alt46) {
						case 1 :
							// smaliParser.g:797:81: VOID_TYPE
							{
							VOID_TYPE252=(Token)match(input,VOID_TYPE,FOLLOW_VOID_TYPE_in_local_directive3742);
							stream_VOID_TYPE.add(VOID_TYPE252);

							}
							break;
						case 2 :
							// smaliParser.g:797:93: nonvoid_type_descriptor
							{
							pushFollow(FOLLOW_nonvoid_type_descriptor_in_local_directive3746);
							nonvoid_type_descriptor253=nonvoid_type_descriptor();
							state._fsp--;

							stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor253.getTree());
							}
							break;

					}

					// smaliParser.g:798:31: ( COMMA signature= STRING_LITERAL )?
					int alt47=2;
					int LA47_0 = input.LA(1);
					if ( (LA47_0==COMMA) ) {
						alt47=1;
					}
					switch (alt47) {
						case 1 :
							// smaliParser.g:798:32: COMMA signature= STRING_LITERAL
							{
							COMMA254=(Token)match(input,COMMA,FOLLOW_COMMA_in_local_directive3780);
							stream_COMMA.add(COMMA254);

							signature=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_local_directive3784);
							stream_STRING_LITERAL.add(signature);

							}
							break;

					}

					}
					break;

			}

			// AST REWRITE
			// elements: name, nonvoid_type_descriptor, signature, NULL_LITERAL, REGISTER
			// token labels: signature, name
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleTokenStream stream_signature=new RewriteRuleTokenStream(adaptor,"token signature",signature);
			RewriteRuleTokenStream stream_name=new RewriteRuleTokenStream(adaptor,"token name",name);
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 799:5: -> ^( I_LOCAL[$start, \"I_LOCAL\"] REGISTER ( NULL_LITERAL )? ( $name)? ( nonvoid_type_descriptor )? ( $signature)? )
			{
				// smaliParser.g:799:8: ^( I_LOCAL[$start, \"I_LOCAL\"] REGISTER ( NULL_LITERAL )? ( $name)? ( nonvoid_type_descriptor )? ( $signature)? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_LOCAL, (retval.start), "I_LOCAL"), root_1);
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				// smaliParser.g:799:46: ( NULL_LITERAL )?
				if ( stream_NULL_LITERAL.hasNext() ) {
					adaptor.addChild(root_1, stream_NULL_LITERAL.nextNode());
				}
				stream_NULL_LITERAL.reset();

				// smaliParser.g:799:61: ( $name)?
				if ( stream_name.hasNext() ) {
					adaptor.addChild(root_1, stream_name.nextNode());
				}
				stream_name.reset();

				// smaliParser.g:799:67: ( nonvoid_type_descriptor )?
				if ( stream_nonvoid_type_descriptor.hasNext() ) {
					adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				}
				stream_nonvoid_type_descriptor.reset();

				// smaliParser.g:799:93: ( $signature)?
				if ( stream_signature.hasNext() ) {
					adaptor.addChild(root_1, stream_signature.nextNode());
				}
				stream_signature.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "local_directive"


	public static class end_local_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "end_local_directive"
	// smaliParser.g:801:1: end_local_directive : END_LOCAL_DIRECTIVE REGISTER -> ^( I_END_LOCAL[$start, \"I_END_LOCAL\"] REGISTER ) ;
	public final smaliParser.end_local_directive_return end_local_directive() throws RecognitionException {
		smaliParser.end_local_directive_return retval = new smaliParser.end_local_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token END_LOCAL_DIRECTIVE255=null;
		Token REGISTER256=null;

		CommonTree END_LOCAL_DIRECTIVE255_tree=null;
		CommonTree REGISTER256_tree=null;
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_END_LOCAL_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_LOCAL_DIRECTIVE");

		try {
			// smaliParser.g:802:3: ( END_LOCAL_DIRECTIVE REGISTER -> ^( I_END_LOCAL[$start, \"I_END_LOCAL\"] REGISTER ) )
			// smaliParser.g:802:5: END_LOCAL_DIRECTIVE REGISTER
			{
			END_LOCAL_DIRECTIVE255=(Token)match(input,END_LOCAL_DIRECTIVE,FOLLOW_END_LOCAL_DIRECTIVE_in_end_local_directive3826);
			stream_END_LOCAL_DIRECTIVE.add(END_LOCAL_DIRECTIVE255);

			REGISTER256=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_end_local_directive3828);
			stream_REGISTER.add(REGISTER256);

			// AST REWRITE
			// elements: REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 803:5: -> ^( I_END_LOCAL[$start, \"I_END_LOCAL\"] REGISTER )
			{
				// smaliParser.g:803:8: ^( I_END_LOCAL[$start, \"I_END_LOCAL\"] REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_END_LOCAL, (retval.start), "I_END_LOCAL"), root_1);
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "end_local_directive"


	public static class restart_local_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "restart_local_directive"
	// smaliParser.g:805:1: restart_local_directive : RESTART_LOCAL_DIRECTIVE REGISTER -> ^( I_RESTART_LOCAL[$start, \"I_RESTART_LOCAL\"] REGISTER ) ;
	public final smaliParser.restart_local_directive_return restart_local_directive() throws RecognitionException {
		smaliParser.restart_local_directive_return retval = new smaliParser.restart_local_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token RESTART_LOCAL_DIRECTIVE257=null;
		Token REGISTER258=null;

		CommonTree RESTART_LOCAL_DIRECTIVE257_tree=null;
		CommonTree REGISTER258_tree=null;
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_RESTART_LOCAL_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token RESTART_LOCAL_DIRECTIVE");

		try {
			// smaliParser.g:806:3: ( RESTART_LOCAL_DIRECTIVE REGISTER -> ^( I_RESTART_LOCAL[$start, \"I_RESTART_LOCAL\"] REGISTER ) )
			// smaliParser.g:806:5: RESTART_LOCAL_DIRECTIVE REGISTER
			{
			RESTART_LOCAL_DIRECTIVE257=(Token)match(input,RESTART_LOCAL_DIRECTIVE,FOLLOW_RESTART_LOCAL_DIRECTIVE_in_restart_local_directive3851);
			stream_RESTART_LOCAL_DIRECTIVE.add(RESTART_LOCAL_DIRECTIVE257);

			REGISTER258=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_restart_local_directive3853);
			stream_REGISTER.add(REGISTER258);

			// AST REWRITE
			// elements: REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 807:5: -> ^( I_RESTART_LOCAL[$start, \"I_RESTART_LOCAL\"] REGISTER )
			{
				// smaliParser.g:807:8: ^( I_RESTART_LOCAL[$start, \"I_RESTART_LOCAL\"] REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_RESTART_LOCAL, (retval.start), "I_RESTART_LOCAL"), root_1);
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "restart_local_directive"


	public static class prologue_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "prologue_directive"
	// smaliParser.g:809:1: prologue_directive : PROLOGUE_DIRECTIVE -> ^( I_PROLOGUE[$start, \"I_PROLOGUE\"] ) ;
	public final smaliParser.prologue_directive_return prologue_directive() throws RecognitionException {
		smaliParser.prologue_directive_return retval = new smaliParser.prologue_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PROLOGUE_DIRECTIVE259=null;

		CommonTree PROLOGUE_DIRECTIVE259_tree=null;
		RewriteRuleTokenStream stream_PROLOGUE_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token PROLOGUE_DIRECTIVE");

		try {
			// smaliParser.g:810:3: ( PROLOGUE_DIRECTIVE -> ^( I_PROLOGUE[$start, \"I_PROLOGUE\"] ) )
			// smaliParser.g:810:5: PROLOGUE_DIRECTIVE
			{
			PROLOGUE_DIRECTIVE259=(Token)match(input,PROLOGUE_DIRECTIVE,FOLLOW_PROLOGUE_DIRECTIVE_in_prologue_directive3876);
			stream_PROLOGUE_DIRECTIVE.add(PROLOGUE_DIRECTIVE259);

			// AST REWRITE
			// elements:
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 811:5: -> ^( I_PROLOGUE[$start, \"I_PROLOGUE\"] )
			{
				// smaliParser.g:811:8: ^( I_PROLOGUE[$start, \"I_PROLOGUE\"] )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PROLOGUE, (retval.start), "I_PROLOGUE"), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "prologue_directive"


	public static class epilogue_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "epilogue_directive"
	// smaliParser.g:813:1: epilogue_directive : EPILOGUE_DIRECTIVE -> ^( I_EPILOGUE[$start, \"I_EPILOGUE\"] ) ;
	public final smaliParser.epilogue_directive_return epilogue_directive() throws RecognitionException {
		smaliParser.epilogue_directive_return retval = new smaliParser.epilogue_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token EPILOGUE_DIRECTIVE260=null;

		CommonTree EPILOGUE_DIRECTIVE260_tree=null;
		RewriteRuleTokenStream stream_EPILOGUE_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token EPILOGUE_DIRECTIVE");

		try {
			// smaliParser.g:814:3: ( EPILOGUE_DIRECTIVE -> ^( I_EPILOGUE[$start, \"I_EPILOGUE\"] ) )
			// smaliParser.g:814:5: EPILOGUE_DIRECTIVE
			{
			EPILOGUE_DIRECTIVE260=(Token)match(input,EPILOGUE_DIRECTIVE,FOLLOW_EPILOGUE_DIRECTIVE_in_epilogue_directive3897);
			stream_EPILOGUE_DIRECTIVE.add(EPILOGUE_DIRECTIVE260);

			// AST REWRITE
			// elements:
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 815:5: -> ^( I_EPILOGUE[$start, \"I_EPILOGUE\"] )
			{
				// smaliParser.g:815:8: ^( I_EPILOGUE[$start, \"I_EPILOGUE\"] )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_EPILOGUE, (retval.start), "I_EPILOGUE"), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "epilogue_directive"


	public static class source_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "source_directive"
	// smaliParser.g:817:1: source_directive : SOURCE_DIRECTIVE ( STRING_LITERAL )? -> ^( I_SOURCE[$start, \"I_SOURCE\"] ( STRING_LITERAL )? ) ;
	public final smaliParser.source_directive_return source_directive() throws RecognitionException {
		smaliParser.source_directive_return retval = new smaliParser.source_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SOURCE_DIRECTIVE261=null;
		Token STRING_LITERAL262=null;

		CommonTree SOURCE_DIRECTIVE261_tree=null;
		CommonTree STRING_LITERAL262_tree=null;
		RewriteRuleTokenStream stream_SOURCE_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token SOURCE_DIRECTIVE");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");

		try {
			// smaliParser.g:818:3: ( SOURCE_DIRECTIVE ( STRING_LITERAL )? -> ^( I_SOURCE[$start, \"I_SOURCE\"] ( STRING_LITERAL )? ) )
			// smaliParser.g:818:5: SOURCE_DIRECTIVE ( STRING_LITERAL )?
			{
			SOURCE_DIRECTIVE261=(Token)match(input,SOURCE_DIRECTIVE,FOLLOW_SOURCE_DIRECTIVE_in_source_directive3918);
			stream_SOURCE_DIRECTIVE.add(SOURCE_DIRECTIVE261);

			// smaliParser.g:818:22: ( STRING_LITERAL )?
			int alt49=2;
			int LA49_0 = input.LA(1);
			if ( (LA49_0==STRING_LITERAL) ) {
				alt49=1;
			}
			switch (alt49) {
				case 1 :
					// smaliParser.g:818:22: STRING_LITERAL
					{
					STRING_LITERAL262=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_source_directive3920);
					stream_STRING_LITERAL.add(STRING_LITERAL262);

					}
					break;

			}

			// AST REWRITE
			// elements: STRING_LITERAL
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 819:5: -> ^( I_SOURCE[$start, \"I_SOURCE\"] ( STRING_LITERAL )? )
			{
				// smaliParser.g:819:8: ^( I_SOURCE[$start, \"I_SOURCE\"] ( STRING_LITERAL )? )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SOURCE, (retval.start), "I_SOURCE"), root_1);
				// smaliParser.g:819:39: ( STRING_LITERAL )?
				if ( stream_STRING_LITERAL.hasNext() ) {
					adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
				}
				stream_STRING_LITERAL.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "source_directive"


	public static class instruction_format12x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "instruction_format12x"
	// smaliParser.g:821:1: instruction_format12x : ( INSTRUCTION_FORMAT12x | INSTRUCTION_FORMAT12x_OR_ID -> INSTRUCTION_FORMAT12x[$INSTRUCTION_FORMAT12x_OR_ID] );
	public final smaliParser.instruction_format12x_return instruction_format12x() throws RecognitionException {
		smaliParser.instruction_format12x_return retval = new smaliParser.instruction_format12x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT12x263=null;
		Token INSTRUCTION_FORMAT12x_OR_ID264=null;

		CommonTree INSTRUCTION_FORMAT12x263_tree=null;
		CommonTree INSTRUCTION_FORMAT12x_OR_ID264_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT12x_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT12x_OR_ID");

		try {
			// smaliParser.g:822:3: ( INSTRUCTION_FORMAT12x | INSTRUCTION_FORMAT12x_OR_ID -> INSTRUCTION_FORMAT12x[$INSTRUCTION_FORMAT12x_OR_ID] )
			int alt50=2;
			int LA50_0 = input.LA(1);
			if ( (LA50_0==INSTRUCTION_FORMAT12x) ) {
				alt50=1;
			}
			else if ( (LA50_0==INSTRUCTION_FORMAT12x_OR_ID) ) {
				alt50=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 50, 0, input);
				throw nvae;
			}

			switch (alt50) {
				case 1 :
					// smaliParser.g:822:5: INSTRUCTION_FORMAT12x
					{
					root_0 = (CommonTree)adaptor.nil();


					INSTRUCTION_FORMAT12x263=(Token)match(input,INSTRUCTION_FORMAT12x,FOLLOW_INSTRUCTION_FORMAT12x_in_instruction_format12x3945);
					INSTRUCTION_FORMAT12x263_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT12x263);
					adaptor.addChild(root_0, INSTRUCTION_FORMAT12x263_tree);

					}
					break;
				case 2 :
					// smaliParser.g:823:5: INSTRUCTION_FORMAT12x_OR_ID
					{
					INSTRUCTION_FORMAT12x_OR_ID264=(Token)match(input,INSTRUCTION_FORMAT12x_OR_ID,FOLLOW_INSTRUCTION_FORMAT12x_OR_ID_in_instruction_format12x3951);
					stream_INSTRUCTION_FORMAT12x_OR_ID.add(INSTRUCTION_FORMAT12x_OR_ID264);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 823:33: -> INSTRUCTION_FORMAT12x[$INSTRUCTION_FORMAT12x_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INSTRUCTION_FORMAT12x, INSTRUCTION_FORMAT12x_OR_ID264));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction_format12x"


	public static class instruction_format22s_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "instruction_format22s"
	// smaliParser.g:825:1: instruction_format22s : ( INSTRUCTION_FORMAT22s | INSTRUCTION_FORMAT22s_OR_ID -> INSTRUCTION_FORMAT22s[$INSTRUCTION_FORMAT22s_OR_ID] );
	public final smaliParser.instruction_format22s_return instruction_format22s() throws RecognitionException {
		smaliParser.instruction_format22s_return retval = new smaliParser.instruction_format22s_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22s265=null;
		Token INSTRUCTION_FORMAT22s_OR_ID266=null;

		CommonTree INSTRUCTION_FORMAT22s265_tree=null;
		CommonTree INSTRUCTION_FORMAT22s_OR_ID266_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22s_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22s_OR_ID");

		try {
			// smaliParser.g:826:3: ( INSTRUCTION_FORMAT22s | INSTRUCTION_FORMAT22s_OR_ID -> INSTRUCTION_FORMAT22s[$INSTRUCTION_FORMAT22s_OR_ID] )
			int alt51=2;
			int LA51_0 = input.LA(1);
			if ( (LA51_0==INSTRUCTION_FORMAT22s) ) {
				alt51=1;
			}
			else if ( (LA51_0==INSTRUCTION_FORMAT22s_OR_ID) ) {
				alt51=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 51, 0, input);
				throw nvae;
			}

			switch (alt51) {
				case 1 :
					// smaliParser.g:826:5: INSTRUCTION_FORMAT22s
					{
					root_0 = (CommonTree)adaptor.nil();


					INSTRUCTION_FORMAT22s265=(Token)match(input,INSTRUCTION_FORMAT22s,FOLLOW_INSTRUCTION_FORMAT22s_in_instruction_format22s3966);
					INSTRUCTION_FORMAT22s265_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT22s265);
					adaptor.addChild(root_0, INSTRUCTION_FORMAT22s265_tree);

					}
					break;
				case 2 :
					// smaliParser.g:827:5: INSTRUCTION_FORMAT22s_OR_ID
					{
					INSTRUCTION_FORMAT22s_OR_ID266=(Token)match(input,INSTRUCTION_FORMAT22s_OR_ID,FOLLOW_INSTRUCTION_FORMAT22s_OR_ID_in_instruction_format22s3972);
					stream_INSTRUCTION_FORMAT22s_OR_ID.add(INSTRUCTION_FORMAT22s_OR_ID266);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 827:33: -> INSTRUCTION_FORMAT22s[$INSTRUCTION_FORMAT22s_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INSTRUCTION_FORMAT22s, INSTRUCTION_FORMAT22s_OR_ID266));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction_format22s"


	public static class instruction_format31i_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "instruction_format31i"
	// smaliParser.g:829:1: instruction_format31i : ( INSTRUCTION_FORMAT31i | INSTRUCTION_FORMAT31i_OR_ID -> INSTRUCTION_FORMAT31i[$INSTRUCTION_FORMAT31i_OR_ID] );
	public final smaliParser.instruction_format31i_return instruction_format31i() throws RecognitionException {
		smaliParser.instruction_format31i_return retval = new smaliParser.instruction_format31i_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT31i267=null;
		Token INSTRUCTION_FORMAT31i_OR_ID268=null;

		CommonTree INSTRUCTION_FORMAT31i267_tree=null;
		CommonTree INSTRUCTION_FORMAT31i_OR_ID268_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT31i_OR_ID=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT31i_OR_ID");

		try {
			// smaliParser.g:830:3: ( INSTRUCTION_FORMAT31i | INSTRUCTION_FORMAT31i_OR_ID -> INSTRUCTION_FORMAT31i[$INSTRUCTION_FORMAT31i_OR_ID] )
			int alt52=2;
			int LA52_0 = input.LA(1);
			if ( (LA52_0==INSTRUCTION_FORMAT31i) ) {
				alt52=1;
			}
			else if ( (LA52_0==INSTRUCTION_FORMAT31i_OR_ID) ) {
				alt52=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 52, 0, input);
				throw nvae;
			}

			switch (alt52) {
				case 1 :
					// smaliParser.g:830:5: INSTRUCTION_FORMAT31i
					{
					root_0 = (CommonTree)adaptor.nil();


					INSTRUCTION_FORMAT31i267=(Token)match(input,INSTRUCTION_FORMAT31i,FOLLOW_INSTRUCTION_FORMAT31i_in_instruction_format31i3987);
					INSTRUCTION_FORMAT31i267_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT31i267);
					adaptor.addChild(root_0, INSTRUCTION_FORMAT31i267_tree);

					}
					break;
				case 2 :
					// smaliParser.g:831:5: INSTRUCTION_FORMAT31i_OR_ID
					{
					INSTRUCTION_FORMAT31i_OR_ID268=(Token)match(input,INSTRUCTION_FORMAT31i_OR_ID,FOLLOW_INSTRUCTION_FORMAT31i_OR_ID_in_instruction_format31i3993);
					stream_INSTRUCTION_FORMAT31i_OR_ID.add(INSTRUCTION_FORMAT31i_OR_ID268);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 831:33: -> INSTRUCTION_FORMAT31i[$INSTRUCTION_FORMAT31i_OR_ID]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INSTRUCTION_FORMAT31i, INSTRUCTION_FORMAT31i_OR_ID268));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction_format31i"


	public static class instruction_format35c_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "instruction_format35c_method"
	// smaliParser.g:833:1: instruction_format35c_method : ( INSTRUCTION_FORMAT35c_METHOD | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE -> INSTRUCTION_FORMAT35c_METHOD[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE] );
	public final smaliParser.instruction_format35c_method_return instruction_format35c_method() throws RecognitionException {
		smaliParser.instruction_format35c_method_return retval = new smaliParser.instruction_format35c_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35c_METHOD269=null;
		Token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE270=null;

		CommonTree INSTRUCTION_FORMAT35c_METHOD269_tree=null;
		CommonTree INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE270_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE");

		try {
			// smaliParser.g:834:3: ( INSTRUCTION_FORMAT35c_METHOD | INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE -> INSTRUCTION_FORMAT35c_METHOD[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE] )
			int alt53=2;
			int LA53_0 = input.LA(1);
			if ( (LA53_0==INSTRUCTION_FORMAT35c_METHOD) ) {
				alt53=1;
			}
			else if ( (LA53_0==INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE) ) {
				alt53=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 53, 0, input);
				throw nvae;
			}

			switch (alt53) {
				case 1 :
					// smaliParser.g:834:5: INSTRUCTION_FORMAT35c_METHOD
					{
					root_0 = (CommonTree)adaptor.nil();


					INSTRUCTION_FORMAT35c_METHOD269=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_instruction_format35c_method4010);
					INSTRUCTION_FORMAT35c_METHOD269_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT35c_METHOD269);
					adaptor.addChild(root_0, INSTRUCTION_FORMAT35c_METHOD269_tree);

					}
					break;
				case 2 :
					// smaliParser.g:835:5: INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE
					{
					INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE270=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_instruction_format35c_method4016);
					stream_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE.add(INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE270);

					// AST REWRITE
					// elements:
					// token labels:
					// rule labels: retval
					// token list labels:
					// rule list labels:
					// wildcard labels:
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (CommonTree)adaptor.nil();
					// 835:56: -> INSTRUCTION_FORMAT35c_METHOD[$INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE]
					{
						adaptor.addChild(root_0, (CommonTree)adaptor.create(INSTRUCTION_FORMAT35c_METHOD, INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE270));
					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction_format35c_method"


	public static class instruction_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "instruction"
	// smaliParser.g:837:1: instruction : ( insn_format10t | insn_format10x | insn_format10x_odex | insn_format11n | insn_format11x | insn_format12x | insn_format20bc | insn_format20t | insn_format21c_field | insn_format21c_field_odex | insn_format21c_method_handle | insn_format21c_method_type | insn_format21c_string | insn_format21c_type | insn_format21ih | insn_format21lh | insn_format21s | insn_format21t | insn_format22b | insn_format22c_field | insn_format22c_field_odex | insn_format22c_type | insn_format22cs_field | insn_format22s | insn_format22t | insn_format22x | insn_format23x | insn_format30t | insn_format31c | insn_format31i | insn_format31t | insn_format32x | insn_format35c_call_site | insn_format35c_method | insn_format35c_type | insn_format35c_method_odex | insn_format35mi_method | insn_format35ms_method | insn_format3rc_call_site | insn_format3rc_method | insn_format3rc_method_odex | insn_format3rc_type | insn_format3rmi_method | insn_format3rms_method | insn_format45cc_method | insn_format4rcc_method | insn_format51l | insn_array_data_directive | insn_packed_switch_directive | insn_sparse_switch_directive );
	public final smaliParser.instruction_return instruction() throws RecognitionException {
		smaliParser.instruction_return retval = new smaliParser.instruction_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		ParserRuleReturnScope insn_format10t271 =null;
		ParserRuleReturnScope insn_format10x272 =null;
		ParserRuleReturnScope insn_format10x_odex273 =null;
		ParserRuleReturnScope insn_format11n274 =null;
		ParserRuleReturnScope insn_format11x275 =null;
		ParserRuleReturnScope insn_format12x276 =null;
		ParserRuleReturnScope insn_format20bc277 =null;
		ParserRuleReturnScope insn_format20t278 =null;
		ParserRuleReturnScope insn_format21c_field279 =null;
		ParserRuleReturnScope insn_format21c_field_odex280 =null;
		ParserRuleReturnScope insn_format21c_method_handle281 =null;
		ParserRuleReturnScope insn_format21c_method_type282 =null;
		ParserRuleReturnScope insn_format21c_string283 =null;
		ParserRuleReturnScope insn_format21c_type284 =null;
		ParserRuleReturnScope insn_format21ih285 =null;
		ParserRuleReturnScope insn_format21lh286 =null;
		ParserRuleReturnScope insn_format21s287 =null;
		ParserRuleReturnScope insn_format21t288 =null;
		ParserRuleReturnScope insn_format22b289 =null;
		ParserRuleReturnScope insn_format22c_field290 =null;
		ParserRuleReturnScope insn_format22c_field_odex291 =null;
		ParserRuleReturnScope insn_format22c_type292 =null;
		ParserRuleReturnScope insn_format22cs_field293 =null;
		ParserRuleReturnScope insn_format22s294 =null;
		ParserRuleReturnScope insn_format22t295 =null;
		ParserRuleReturnScope insn_format22x296 =null;
		ParserRuleReturnScope insn_format23x297 =null;
		ParserRuleReturnScope insn_format30t298 =null;
		ParserRuleReturnScope insn_format31c299 =null;
		ParserRuleReturnScope insn_format31i300 =null;
		ParserRuleReturnScope insn_format31t301 =null;
		ParserRuleReturnScope insn_format32x302 =null;
		ParserRuleReturnScope insn_format35c_call_site303 =null;
		ParserRuleReturnScope insn_format35c_method304 =null;
		ParserRuleReturnScope insn_format35c_type305 =null;
		ParserRuleReturnScope insn_format35c_method_odex306 =null;
		ParserRuleReturnScope insn_format35mi_method307 =null;
		ParserRuleReturnScope insn_format35ms_method308 =null;
		ParserRuleReturnScope insn_format3rc_call_site309 =null;
		ParserRuleReturnScope insn_format3rc_method310 =null;
		ParserRuleReturnScope insn_format3rc_method_odex311 =null;
		ParserRuleReturnScope insn_format3rc_type312 =null;
		ParserRuleReturnScope insn_format3rmi_method313 =null;
		ParserRuleReturnScope insn_format3rms_method314 =null;
		ParserRuleReturnScope insn_format45cc_method315 =null;
		ParserRuleReturnScope insn_format4rcc_method316 =null;
		ParserRuleReturnScope insn_format51l317 =null;
		ParserRuleReturnScope insn_array_data_directive318 =null;
		ParserRuleReturnScope insn_packed_switch_directive319 =null;
		ParserRuleReturnScope insn_sparse_switch_directive320 =null;


		try {
			// smaliParser.g:838:3: ( insn_format10t | insn_format10x | insn_format10x_odex | insn_format11n | insn_format11x | insn_format12x | insn_format20bc | insn_format20t | insn_format21c_field | insn_format21c_field_odex | insn_format21c_method_handle | insn_format21c_method_type | insn_format21c_string | insn_format21c_type | insn_format21ih | insn_format21lh | insn_format21s | insn_format21t | insn_format22b | insn_format22c_field | insn_format22c_field_odex | insn_format22c_type | insn_format22cs_field | insn_format22s | insn_format22t | insn_format22x | insn_format23x | insn_format30t | insn_format31c | insn_format31i | insn_format31t | insn_format32x | insn_format35c_call_site | insn_format35c_method | insn_format35c_type | insn_format35c_method_odex | insn_format35mi_method | insn_format35ms_method | insn_format3rc_call_site | insn_format3rc_method | insn_format3rc_method_odex | insn_format3rc_type | insn_format3rmi_method | insn_format3rms_method | insn_format45cc_method | insn_format4rcc_method | insn_format51l | insn_array_data_directive | insn_packed_switch_directive | insn_sparse_switch_directive )
			int alt54=50;
			switch ( input.LA(1) ) {
			case INSTRUCTION_FORMAT10t:
				{
				alt54=1;
				}
				break;
			case INSTRUCTION_FORMAT10x:
				{
				alt54=2;
				}
				break;
			case INSTRUCTION_FORMAT10x_ODEX:
				{
				alt54=3;
				}
				break;
			case INSTRUCTION_FORMAT11n:
				{
				alt54=4;
				}
				break;
			case INSTRUCTION_FORMAT11x:
				{
				alt54=5;
				}
				break;
			case INSTRUCTION_FORMAT12x:
			case INSTRUCTION_FORMAT12x_OR_ID:
				{
				alt54=6;
				}
				break;
			case INSTRUCTION_FORMAT20bc:
				{
				alt54=7;
				}
				break;
			case INSTRUCTION_FORMAT20t:
				{
				alt54=8;
				}
				break;
			case INSTRUCTION_FORMAT21c_FIELD:
				{
				alt54=9;
				}
				break;
			case INSTRUCTION_FORMAT21c_FIELD_ODEX:
				{
				alt54=10;
				}
				break;
			case INSTRUCTION_FORMAT21c_METHOD_HANDLE:
				{
				alt54=11;
				}
				break;
			case INSTRUCTION_FORMAT21c_METHOD_TYPE:
				{
				alt54=12;
				}
				break;
			case INSTRUCTION_FORMAT21c_STRING:
				{
				alt54=13;
				}
				break;
			case INSTRUCTION_FORMAT21c_TYPE:
				{
				alt54=14;
				}
				break;
			case INSTRUCTION_FORMAT21ih:
				{
				alt54=15;
				}
				break;
			case INSTRUCTION_FORMAT21lh:
				{
				alt54=16;
				}
				break;
			case INSTRUCTION_FORMAT21s:
				{
				alt54=17;
				}
				break;
			case INSTRUCTION_FORMAT21t:
				{
				alt54=18;
				}
				break;
			case INSTRUCTION_FORMAT22b:
				{
				alt54=19;
				}
				break;
			case INSTRUCTION_FORMAT22c_FIELD:
				{
				alt54=20;
				}
				break;
			case INSTRUCTION_FORMAT22c_FIELD_ODEX:
				{
				alt54=21;
				}
				break;
			case INSTRUCTION_FORMAT22c_TYPE:
				{
				alt54=22;
				}
				break;
			case INSTRUCTION_FORMAT22cs_FIELD:
				{
				alt54=23;
				}
				break;
			case INSTRUCTION_FORMAT22s:
			case INSTRUCTION_FORMAT22s_OR_ID:
				{
				alt54=24;
				}
				break;
			case INSTRUCTION_FORMAT22t:
				{
				alt54=25;
				}
				break;
			case INSTRUCTION_FORMAT22x:
				{
				alt54=26;
				}
				break;
			case INSTRUCTION_FORMAT23x:
				{
				alt54=27;
				}
				break;
			case INSTRUCTION_FORMAT30t:
				{
				alt54=28;
				}
				break;
			case INSTRUCTION_FORMAT31c:
				{
				alt54=29;
				}
				break;
			case INSTRUCTION_FORMAT31i:
			case INSTRUCTION_FORMAT31i_OR_ID:
				{
				alt54=30;
				}
				break;
			case INSTRUCTION_FORMAT31t:
				{
				alt54=31;
				}
				break;
			case INSTRUCTION_FORMAT32x:
				{
				alt54=32;
				}
				break;
			case INSTRUCTION_FORMAT35c_CALL_SITE:
				{
				alt54=33;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD:
			case INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE:
				{
				alt54=34;
				}
				break;
			case INSTRUCTION_FORMAT35c_TYPE:
				{
				alt54=35;
				}
				break;
			case INSTRUCTION_FORMAT35c_METHOD_ODEX:
				{
				alt54=36;
				}
				break;
			case INSTRUCTION_FORMAT35mi_METHOD:
				{
				alt54=37;
				}
				break;
			case INSTRUCTION_FORMAT35ms_METHOD:
				{
				alt54=38;
				}
				break;
			case INSTRUCTION_FORMAT3rc_CALL_SITE:
				{
				alt54=39;
				}
				break;
			case INSTRUCTION_FORMAT3rc_METHOD:
				{
				alt54=40;
				}
				break;
			case INSTRUCTION_FORMAT3rc_METHOD_ODEX:
				{
				alt54=41;
				}
				break;
			case INSTRUCTION_FORMAT3rc_TYPE:
				{
				alt54=42;
				}
				break;
			case INSTRUCTION_FORMAT3rmi_METHOD:
				{
				alt54=43;
				}
				break;
			case INSTRUCTION_FORMAT3rms_METHOD:
				{
				alt54=44;
				}
				break;
			case INSTRUCTION_FORMAT45cc_METHOD:
				{
				alt54=45;
				}
				break;
			case INSTRUCTION_FORMAT4rcc_METHOD:
				{
				alt54=46;
				}
				break;
			case INSTRUCTION_FORMAT51l:
				{
				alt54=47;
				}
				break;
			case ARRAY_DATA_DIRECTIVE:
				{
				alt54=48;
				}
				break;
			case PACKED_SWITCH_DIRECTIVE:
				{
				alt54=49;
				}
				break;
			case SPARSE_SWITCH_DIRECTIVE:
				{
				alt54=50;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 54, 0, input);
				throw nvae;
			}
			switch (alt54) {
				case 1 :
					// smaliParser.g:838:5: insn_format10t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format10t_in_instruction4031);
					insn_format10t271=insn_format10t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format10t271.getTree());

					}
					break;
				case 2 :
					// smaliParser.g:839:5: insn_format10x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format10x_in_instruction4037);
					insn_format10x272=insn_format10x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format10x272.getTree());

					}
					break;
				case 3 :
					// smaliParser.g:840:5: insn_format10x_odex
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format10x_odex_in_instruction4043);
					insn_format10x_odex273=insn_format10x_odex();
					state._fsp--;

					adaptor.addChild(root_0, insn_format10x_odex273.getTree());

					}
					break;
				case 4 :
					// smaliParser.g:841:5: insn_format11n
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format11n_in_instruction4049);
					insn_format11n274=insn_format11n();
					state._fsp--;

					adaptor.addChild(root_0, insn_format11n274.getTree());

					}
					break;
				case 5 :
					// smaliParser.g:842:5: insn_format11x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format11x_in_instruction4055);
					insn_format11x275=insn_format11x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format11x275.getTree());

					}
					break;
				case 6 :
					// smaliParser.g:843:5: insn_format12x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format12x_in_instruction4061);
					insn_format12x276=insn_format12x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format12x276.getTree());

					}
					break;
				case 7 :
					// smaliParser.g:844:5: insn_format20bc
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format20bc_in_instruction4067);
					insn_format20bc277=insn_format20bc();
					state._fsp--;

					adaptor.addChild(root_0, insn_format20bc277.getTree());

					}
					break;
				case 8 :
					// smaliParser.g:845:5: insn_format20t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format20t_in_instruction4073);
					insn_format20t278=insn_format20t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format20t278.getTree());

					}
					break;
				case 9 :
					// smaliParser.g:846:5: insn_format21c_field
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_field_in_instruction4079);
					insn_format21c_field279=insn_format21c_field();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_field279.getTree());

					}
					break;
				case 10 :
					// smaliParser.g:847:5: insn_format21c_field_odex
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_field_odex_in_instruction4085);
					insn_format21c_field_odex280=insn_format21c_field_odex();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_field_odex280.getTree());

					}
					break;
				case 11 :
					// smaliParser.g:848:5: insn_format21c_method_handle
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_method_handle_in_instruction4091);
					insn_format21c_method_handle281=insn_format21c_method_handle();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_method_handle281.getTree());

					}
					break;
				case 12 :
					// smaliParser.g:849:5: insn_format21c_method_type
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_method_type_in_instruction4097);
					insn_format21c_method_type282=insn_format21c_method_type();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_method_type282.getTree());

					}
					break;
				case 13 :
					// smaliParser.g:850:5: insn_format21c_string
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_string_in_instruction4103);
					insn_format21c_string283=insn_format21c_string();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_string283.getTree());

					}
					break;
				case 14 :
					// smaliParser.g:851:5: insn_format21c_type
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21c_type_in_instruction4109);
					insn_format21c_type284=insn_format21c_type();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21c_type284.getTree());

					}
					break;
				case 15 :
					// smaliParser.g:852:5: insn_format21ih
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21ih_in_instruction4115);
					insn_format21ih285=insn_format21ih();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21ih285.getTree());

					}
					break;
				case 16 :
					// smaliParser.g:853:5: insn_format21lh
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21lh_in_instruction4121);
					insn_format21lh286=insn_format21lh();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21lh286.getTree());

					}
					break;
				case 17 :
					// smaliParser.g:854:5: insn_format21s
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21s_in_instruction4127);
					insn_format21s287=insn_format21s();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21s287.getTree());

					}
					break;
				case 18 :
					// smaliParser.g:855:5: insn_format21t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format21t_in_instruction4133);
					insn_format21t288=insn_format21t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format21t288.getTree());

					}
					break;
				case 19 :
					// smaliParser.g:856:5: insn_format22b
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22b_in_instruction4139);
					insn_format22b289=insn_format22b();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22b289.getTree());

					}
					break;
				case 20 :
					// smaliParser.g:857:5: insn_format22c_field
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22c_field_in_instruction4145);
					insn_format22c_field290=insn_format22c_field();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22c_field290.getTree());

					}
					break;
				case 21 :
					// smaliParser.g:858:5: insn_format22c_field_odex
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22c_field_odex_in_instruction4151);
					insn_format22c_field_odex291=insn_format22c_field_odex();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22c_field_odex291.getTree());

					}
					break;
				case 22 :
					// smaliParser.g:859:5: insn_format22c_type
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22c_type_in_instruction4157);
					insn_format22c_type292=insn_format22c_type();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22c_type292.getTree());

					}
					break;
				case 23 :
					// smaliParser.g:860:5: insn_format22cs_field
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22cs_field_in_instruction4163);
					insn_format22cs_field293=insn_format22cs_field();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22cs_field293.getTree());

					}
					break;
				case 24 :
					// smaliParser.g:861:5: insn_format22s
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22s_in_instruction4169);
					insn_format22s294=insn_format22s();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22s294.getTree());

					}
					break;
				case 25 :
					// smaliParser.g:862:5: insn_format22t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22t_in_instruction4175);
					insn_format22t295=insn_format22t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22t295.getTree());

					}
					break;
				case 26 :
					// smaliParser.g:863:5: insn_format22x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format22x_in_instruction4181);
					insn_format22x296=insn_format22x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format22x296.getTree());

					}
					break;
				case 27 :
					// smaliParser.g:864:5: insn_format23x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format23x_in_instruction4187);
					insn_format23x297=insn_format23x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format23x297.getTree());

					}
					break;
				case 28 :
					// smaliParser.g:865:5: insn_format30t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format30t_in_instruction4193);
					insn_format30t298=insn_format30t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format30t298.getTree());

					}
					break;
				case 29 :
					// smaliParser.g:866:5: insn_format31c
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format31c_in_instruction4199);
					insn_format31c299=insn_format31c();
					state._fsp--;

					adaptor.addChild(root_0, insn_format31c299.getTree());

					}
					break;
				case 30 :
					// smaliParser.g:867:5: insn_format31i
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format31i_in_instruction4205);
					insn_format31i300=insn_format31i();
					state._fsp--;

					adaptor.addChild(root_0, insn_format31i300.getTree());

					}
					break;
				case 31 :
					// smaliParser.g:868:5: insn_format31t
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format31t_in_instruction4211);
					insn_format31t301=insn_format31t();
					state._fsp--;

					adaptor.addChild(root_0, insn_format31t301.getTree());

					}
					break;
				case 32 :
					// smaliParser.g:869:5: insn_format32x
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format32x_in_instruction4217);
					insn_format32x302=insn_format32x();
					state._fsp--;

					adaptor.addChild(root_0, insn_format32x302.getTree());

					}
					break;
				case 33 :
					// smaliParser.g:870:5: insn_format35c_call_site
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35c_call_site_in_instruction4223);
					insn_format35c_call_site303=insn_format35c_call_site();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35c_call_site303.getTree());

					}
					break;
				case 34 :
					// smaliParser.g:871:5: insn_format35c_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35c_method_in_instruction4229);
					insn_format35c_method304=insn_format35c_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35c_method304.getTree());

					}
					break;
				case 35 :
					// smaliParser.g:872:5: insn_format35c_type
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35c_type_in_instruction4235);
					insn_format35c_type305=insn_format35c_type();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35c_type305.getTree());

					}
					break;
				case 36 :
					// smaliParser.g:873:5: insn_format35c_method_odex
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35c_method_odex_in_instruction4241);
					insn_format35c_method_odex306=insn_format35c_method_odex();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35c_method_odex306.getTree());

					}
					break;
				case 37 :
					// smaliParser.g:874:5: insn_format35mi_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35mi_method_in_instruction4247);
					insn_format35mi_method307=insn_format35mi_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35mi_method307.getTree());

					}
					break;
				case 38 :
					// smaliParser.g:875:5: insn_format35ms_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format35ms_method_in_instruction4253);
					insn_format35ms_method308=insn_format35ms_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format35ms_method308.getTree());

					}
					break;
				case 39 :
					// smaliParser.g:876:5: insn_format3rc_call_site
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rc_call_site_in_instruction4259);
					insn_format3rc_call_site309=insn_format3rc_call_site();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rc_call_site309.getTree());

					}
					break;
				case 40 :
					// smaliParser.g:877:5: insn_format3rc_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rc_method_in_instruction4265);
					insn_format3rc_method310=insn_format3rc_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rc_method310.getTree());

					}
					break;
				case 41 :
					// smaliParser.g:878:5: insn_format3rc_method_odex
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rc_method_odex_in_instruction4271);
					insn_format3rc_method_odex311=insn_format3rc_method_odex();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rc_method_odex311.getTree());

					}
					break;
				case 42 :
					// smaliParser.g:879:5: insn_format3rc_type
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rc_type_in_instruction4277);
					insn_format3rc_type312=insn_format3rc_type();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rc_type312.getTree());

					}
					break;
				case 43 :
					// smaliParser.g:880:5: insn_format3rmi_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rmi_method_in_instruction4283);
					insn_format3rmi_method313=insn_format3rmi_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rmi_method313.getTree());

					}
					break;
				case 44 :
					// smaliParser.g:881:5: insn_format3rms_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format3rms_method_in_instruction4289);
					insn_format3rms_method314=insn_format3rms_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format3rms_method314.getTree());

					}
					break;
				case 45 :
					// smaliParser.g:882:5: insn_format45cc_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format45cc_method_in_instruction4295);
					insn_format45cc_method315=insn_format45cc_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format45cc_method315.getTree());

					}
					break;
				case 46 :
					// smaliParser.g:883:5: insn_format4rcc_method
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format4rcc_method_in_instruction4301);
					insn_format4rcc_method316=insn_format4rcc_method();
					state._fsp--;

					adaptor.addChild(root_0, insn_format4rcc_method316.getTree());

					}
					break;
				case 47 :
					// smaliParser.g:884:5: insn_format51l
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_format51l_in_instruction4307);
					insn_format51l317=insn_format51l();
					state._fsp--;

					adaptor.addChild(root_0, insn_format51l317.getTree());

					}
					break;
				case 48 :
					// smaliParser.g:885:5: insn_array_data_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_array_data_directive_in_instruction4313);
					insn_array_data_directive318=insn_array_data_directive();
					state._fsp--;

					adaptor.addChild(root_0, insn_array_data_directive318.getTree());

					}
					break;
				case 49 :
					// smaliParser.g:886:5: insn_packed_switch_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_packed_switch_directive_in_instruction4319);
					insn_packed_switch_directive319=insn_packed_switch_directive();
					state._fsp--;

					adaptor.addChild(root_0, insn_packed_switch_directive319.getTree());

					}
					break;
				case 50 :
					// smaliParser.g:887:5: insn_sparse_switch_directive
					{
					root_0 = (CommonTree)adaptor.nil();


					pushFollow(FOLLOW_insn_sparse_switch_directive_in_instruction4325);
					insn_sparse_switch_directive320=insn_sparse_switch_directive();
					state._fsp--;

					adaptor.addChild(root_0, insn_sparse_switch_directive320.getTree());

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "instruction"


	public static class insn_format10t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format10t"
	// smaliParser.g:889:1: insn_format10t : INSTRUCTION_FORMAT10t label_ref -> ^( I_STATEMENT_FORMAT10t[$start, \"I_STATEMENT_FORMAT10t\"] INSTRUCTION_FORMAT10t label_ref ) ;
	public final smaliParser.insn_format10t_return insn_format10t() throws RecognitionException {
		smaliParser.insn_format10t_return retval = new smaliParser.insn_format10t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT10t321=null;
		ParserRuleReturnScope label_ref322 =null;

		CommonTree INSTRUCTION_FORMAT10t321_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT10t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT10t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:890:3: ( INSTRUCTION_FORMAT10t label_ref -> ^( I_STATEMENT_FORMAT10t[$start, \"I_STATEMENT_FORMAT10t\"] INSTRUCTION_FORMAT10t label_ref ) )
			// smaliParser.g:892:5: INSTRUCTION_FORMAT10t label_ref
			{
			INSTRUCTION_FORMAT10t321=(Token)match(input,INSTRUCTION_FORMAT10t,FOLLOW_INSTRUCTION_FORMAT10t_in_insn_format10t4345);
			stream_INSTRUCTION_FORMAT10t.add(INSTRUCTION_FORMAT10t321);

			pushFollow(FOLLOW_label_ref_in_insn_format10t4347);
			label_ref322=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref322.getTree());
			// AST REWRITE
			// elements: label_ref, INSTRUCTION_FORMAT10t
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 893:5: -> ^( I_STATEMENT_FORMAT10t[$start, \"I_STATEMENT_FORMAT10t\"] INSTRUCTION_FORMAT10t label_ref )
			{
				// smaliParser.g:893:8: ^( I_STATEMENT_FORMAT10t[$start, \"I_STATEMENT_FORMAT10t\"] INSTRUCTION_FORMAT10t label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT10t, (retval.start), "I_STATEMENT_FORMAT10t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT10t.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format10t"


	public static class insn_format10x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format10x"
	// smaliParser.g:895:1: insn_format10x : INSTRUCTION_FORMAT10x -> ^( I_STATEMENT_FORMAT10x[$start, \"I_STATEMENT_FORMAT10x\"] INSTRUCTION_FORMAT10x ) ;
	public final smaliParser.insn_format10x_return insn_format10x() throws RecognitionException {
		smaliParser.insn_format10x_return retval = new smaliParser.insn_format10x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT10x323=null;

		CommonTree INSTRUCTION_FORMAT10x323_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT10x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT10x");

		try {
			// smaliParser.g:896:3: ( INSTRUCTION_FORMAT10x -> ^( I_STATEMENT_FORMAT10x[$start, \"I_STATEMENT_FORMAT10x\"] INSTRUCTION_FORMAT10x ) )
			// smaliParser.g:897:5: INSTRUCTION_FORMAT10x
			{
			INSTRUCTION_FORMAT10x323=(Token)match(input,INSTRUCTION_FORMAT10x,FOLLOW_INSTRUCTION_FORMAT10x_in_insn_format10x4377);
			stream_INSTRUCTION_FORMAT10x.add(INSTRUCTION_FORMAT10x323);

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT10x
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 898:5: -> ^( I_STATEMENT_FORMAT10x[$start, \"I_STATEMENT_FORMAT10x\"] INSTRUCTION_FORMAT10x )
			{
				// smaliParser.g:898:8: ^( I_STATEMENT_FORMAT10x[$start, \"I_STATEMENT_FORMAT10x\"] INSTRUCTION_FORMAT10x )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT10x, (retval.start), "I_STATEMENT_FORMAT10x"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT10x.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format10x"


	public static class insn_format10x_odex_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format10x_odex"
	// smaliParser.g:900:1: insn_format10x_odex : INSTRUCTION_FORMAT10x_ODEX ;
	public final smaliParser.insn_format10x_odex_return insn_format10x_odex() throws RecognitionException {
		smaliParser.insn_format10x_odex_return retval = new smaliParser.insn_format10x_odex_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT10x_ODEX324=null;

		CommonTree INSTRUCTION_FORMAT10x_ODEX324_tree=null;

		try {
			// smaliParser.g:901:3: ( INSTRUCTION_FORMAT10x_ODEX )
			// smaliParser.g:902:5: INSTRUCTION_FORMAT10x_ODEX
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT10x_ODEX324=(Token)match(input,INSTRUCTION_FORMAT10x_ODEX,FOLLOW_INSTRUCTION_FORMAT10x_ODEX_in_insn_format10x_odex4405);
			INSTRUCTION_FORMAT10x_ODEX324_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT10x_ODEX324);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT10x_ODEX324_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT10x_ODEX324!=null?INSTRUCTION_FORMAT10x_ODEX324.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format10x_odex"


	public static class insn_format11n_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format11n"
	// smaliParser.g:907:1: insn_format11n : INSTRUCTION_FORMAT11n REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT11n[$start, \"I_STATEMENT_FORMAT11n\"] INSTRUCTION_FORMAT11n REGISTER integral_literal ) ;
	public final smaliParser.insn_format11n_return insn_format11n() throws RecognitionException {
		smaliParser.insn_format11n_return retval = new smaliParser.insn_format11n_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT11n325=null;
		Token REGISTER326=null;
		Token COMMA327=null;
		ParserRuleReturnScope integral_literal328 =null;

		CommonTree INSTRUCTION_FORMAT11n325_tree=null;
		CommonTree REGISTER326_tree=null;
		CommonTree COMMA327_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT11n=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT11n");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:908:3: ( INSTRUCTION_FORMAT11n REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT11n[$start, \"I_STATEMENT_FORMAT11n\"] INSTRUCTION_FORMAT11n REGISTER integral_literal ) )
			// smaliParser.g:909:5: INSTRUCTION_FORMAT11n REGISTER COMMA integral_literal
			{
			INSTRUCTION_FORMAT11n325=(Token)match(input,INSTRUCTION_FORMAT11n,FOLLOW_INSTRUCTION_FORMAT11n_in_insn_format11n4426);
			stream_INSTRUCTION_FORMAT11n.add(INSTRUCTION_FORMAT11n325);

			REGISTER326=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format11n4428);
			stream_REGISTER.add(REGISTER326);

			COMMA327=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format11n4430);
			stream_COMMA.add(COMMA327);

			pushFollow(FOLLOW_integral_literal_in_insn_format11n4432);
			integral_literal328=integral_literal();
			state._fsp--;

			stream_integral_literal.add(integral_literal328.getTree());
			// AST REWRITE
			// elements: integral_literal, INSTRUCTION_FORMAT11n, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 910:5: -> ^( I_STATEMENT_FORMAT11n[$start, \"I_STATEMENT_FORMAT11n\"] INSTRUCTION_FORMAT11n REGISTER integral_literal )
			{
				// smaliParser.g:910:8: ^( I_STATEMENT_FORMAT11n[$start, \"I_STATEMENT_FORMAT11n\"] INSTRUCTION_FORMAT11n REGISTER integral_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT11n, (retval.start), "I_STATEMENT_FORMAT11n"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT11n.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_integral_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format11n"


	public static class insn_format11x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format11x"
	// smaliParser.g:912:1: insn_format11x : INSTRUCTION_FORMAT11x REGISTER -> ^( I_STATEMENT_FORMAT11x[$start, \"I_STATEMENT_FORMAT11x\"] INSTRUCTION_FORMAT11x REGISTER ) ;
	public final smaliParser.insn_format11x_return insn_format11x() throws RecognitionException {
		smaliParser.insn_format11x_return retval = new smaliParser.insn_format11x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT11x329=null;
		Token REGISTER330=null;

		CommonTree INSTRUCTION_FORMAT11x329_tree=null;
		CommonTree REGISTER330_tree=null;
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT11x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT11x");

		try {
			// smaliParser.g:913:3: ( INSTRUCTION_FORMAT11x REGISTER -> ^( I_STATEMENT_FORMAT11x[$start, \"I_STATEMENT_FORMAT11x\"] INSTRUCTION_FORMAT11x REGISTER ) )
			// smaliParser.g:914:5: INSTRUCTION_FORMAT11x REGISTER
			{
			INSTRUCTION_FORMAT11x329=(Token)match(input,INSTRUCTION_FORMAT11x,FOLLOW_INSTRUCTION_FORMAT11x_in_insn_format11x4464);
			stream_INSTRUCTION_FORMAT11x.add(INSTRUCTION_FORMAT11x329);

			REGISTER330=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format11x4466);
			stream_REGISTER.add(REGISTER330);

			// AST REWRITE
			// elements: REGISTER, INSTRUCTION_FORMAT11x
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 915:5: -> ^( I_STATEMENT_FORMAT11x[$start, \"I_STATEMENT_FORMAT11x\"] INSTRUCTION_FORMAT11x REGISTER )
			{
				// smaliParser.g:915:8: ^( I_STATEMENT_FORMAT11x[$start, \"I_STATEMENT_FORMAT11x\"] INSTRUCTION_FORMAT11x REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT11x, (retval.start), "I_STATEMENT_FORMAT11x"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT11x.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format11x"


	public static class insn_format12x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format12x"
	// smaliParser.g:917:1: insn_format12x : instruction_format12x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT12x[$start, \"I_STATEMENT_FORMAT12x\"] instruction_format12x REGISTER REGISTER ) ;
	public final smaliParser.insn_format12x_return insn_format12x() throws RecognitionException {
		smaliParser.insn_format12x_return retval = new smaliParser.insn_format12x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token REGISTER332=null;
		Token COMMA333=null;
		Token REGISTER334=null;
		ParserRuleReturnScope instruction_format12x331 =null;

		CommonTree REGISTER332_tree=null;
		CommonTree COMMA333_tree=null;
		CommonTree REGISTER334_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleSubtreeStream stream_instruction_format12x=new RewriteRuleSubtreeStream(adaptor,"rule instruction_format12x");

		try {
			// smaliParser.g:918:3: ( instruction_format12x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT12x[$start, \"I_STATEMENT_FORMAT12x\"] instruction_format12x REGISTER REGISTER ) )
			// smaliParser.g:919:5: instruction_format12x REGISTER COMMA REGISTER
			{
			pushFollow(FOLLOW_instruction_format12x_in_insn_format12x4496);
			instruction_format12x331=instruction_format12x();
			state._fsp--;

			stream_instruction_format12x.add(instruction_format12x331.getTree());
			REGISTER332=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format12x4498);
			stream_REGISTER.add(REGISTER332);

			COMMA333=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format12x4500);
			stream_COMMA.add(COMMA333);

			REGISTER334=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format12x4502);
			stream_REGISTER.add(REGISTER334);

			// AST REWRITE
			// elements: REGISTER, instruction_format12x, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 920:5: -> ^( I_STATEMENT_FORMAT12x[$start, \"I_STATEMENT_FORMAT12x\"] instruction_format12x REGISTER REGISTER )
			{
				// smaliParser.g:920:8: ^( I_STATEMENT_FORMAT12x[$start, \"I_STATEMENT_FORMAT12x\"] instruction_format12x REGISTER REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT12x, (retval.start), "I_STATEMENT_FORMAT12x"), root_1);
				adaptor.addChild(root_1, stream_instruction_format12x.nextTree());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format12x"


	public static class insn_format20bc_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format20bc"
	// smaliParser.g:922:1: insn_format20bc : INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE COMMA verification_error_reference -> ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE verification_error_reference ) ;
	public final smaliParser.insn_format20bc_return insn_format20bc() throws RecognitionException {
		smaliParser.insn_format20bc_return retval = new smaliParser.insn_format20bc_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT20bc335=null;
		Token VERIFICATION_ERROR_TYPE336=null;
		Token COMMA337=null;
		ParserRuleReturnScope verification_error_reference338 =null;

		CommonTree INSTRUCTION_FORMAT20bc335_tree=null;
		CommonTree VERIFICATION_ERROR_TYPE336_tree=null;
		CommonTree COMMA337_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_VERIFICATION_ERROR_TYPE=new RewriteRuleTokenStream(adaptor,"token VERIFICATION_ERROR_TYPE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT20bc=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT20bc");
		RewriteRuleSubtreeStream stream_verification_error_reference=new RewriteRuleSubtreeStream(adaptor,"rule verification_error_reference");

		try {
			// smaliParser.g:923:3: ( INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE COMMA verification_error_reference -> ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE verification_error_reference ) )
			// smaliParser.g:924:5: INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE COMMA verification_error_reference
			{
			INSTRUCTION_FORMAT20bc335=(Token)match(input,INSTRUCTION_FORMAT20bc,FOLLOW_INSTRUCTION_FORMAT20bc_in_insn_format20bc4534);
			stream_INSTRUCTION_FORMAT20bc.add(INSTRUCTION_FORMAT20bc335);

			VERIFICATION_ERROR_TYPE336=(Token)match(input,VERIFICATION_ERROR_TYPE,FOLLOW_VERIFICATION_ERROR_TYPE_in_insn_format20bc4536);
			stream_VERIFICATION_ERROR_TYPE.add(VERIFICATION_ERROR_TYPE336);

			COMMA337=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format20bc4538);
			stream_COMMA.add(COMMA337);

			pushFollow(FOLLOW_verification_error_reference_in_insn_format20bc4540);
			verification_error_reference338=verification_error_reference();
			state._fsp--;

			stream_verification_error_reference.add(verification_error_reference338.getTree());

			      if (!allowOdex || opcodes.getOpcodeByName((INSTRUCTION_FORMAT20bc335!=null?INSTRUCTION_FORMAT20bc335.getText():null)) == null || apiLevel >= 14) {
			        throwOdexedInstructionException(input, (INSTRUCTION_FORMAT20bc335!=null?INSTRUCTION_FORMAT20bc335.getText():null));
			      }

			// AST REWRITE
			// elements: verification_error_reference, VERIFICATION_ERROR_TYPE, INSTRUCTION_FORMAT20bc
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 930:5: -> ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE verification_error_reference )
			{
				// smaliParser.g:930:8: ^( I_STATEMENT_FORMAT20bc INSTRUCTION_FORMAT20bc VERIFICATION_ERROR_TYPE verification_error_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT20bc, "I_STATEMENT_FORMAT20bc"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT20bc.nextNode());
				adaptor.addChild(root_1, stream_VERIFICATION_ERROR_TYPE.nextNode());
				adaptor.addChild(root_1, stream_verification_error_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format20bc"


	public static class insn_format20t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format20t"
	// smaliParser.g:932:1: insn_format20t : INSTRUCTION_FORMAT20t label_ref -> ^( I_STATEMENT_FORMAT20t[$start, \"I_STATEMENT_FORMAT20t\"] INSTRUCTION_FORMAT20t label_ref ) ;
	public final smaliParser.insn_format20t_return insn_format20t() throws RecognitionException {
		smaliParser.insn_format20t_return retval = new smaliParser.insn_format20t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT20t339=null;
		ParserRuleReturnScope label_ref340 =null;

		CommonTree INSTRUCTION_FORMAT20t339_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT20t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT20t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:933:3: ( INSTRUCTION_FORMAT20t label_ref -> ^( I_STATEMENT_FORMAT20t[$start, \"I_STATEMENT_FORMAT20t\"] INSTRUCTION_FORMAT20t label_ref ) )
			// smaliParser.g:934:5: INSTRUCTION_FORMAT20t label_ref
			{
			INSTRUCTION_FORMAT20t339=(Token)match(input,INSTRUCTION_FORMAT20t,FOLLOW_INSTRUCTION_FORMAT20t_in_insn_format20t4577);
			stream_INSTRUCTION_FORMAT20t.add(INSTRUCTION_FORMAT20t339);

			pushFollow(FOLLOW_label_ref_in_insn_format20t4579);
			label_ref340=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref340.getTree());
			// AST REWRITE
			// elements: label_ref, INSTRUCTION_FORMAT20t
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 935:5: -> ^( I_STATEMENT_FORMAT20t[$start, \"I_STATEMENT_FORMAT20t\"] INSTRUCTION_FORMAT20t label_ref )
			{
				// smaliParser.g:935:8: ^( I_STATEMENT_FORMAT20t[$start, \"I_STATEMENT_FORMAT20t\"] INSTRUCTION_FORMAT20t label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT20t, (retval.start), "I_STATEMENT_FORMAT20t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT20t.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format20t"


	public static class insn_format21c_field_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_field"
	// smaliParser.g:937:1: insn_format21c_field : INSTRUCTION_FORMAT21c_FIELD REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD REGISTER field_reference ) ;
	public final smaliParser.insn_format21c_field_return insn_format21c_field() throws RecognitionException {
		smaliParser.insn_format21c_field_return retval = new smaliParser.insn_format21c_field_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_FIELD341=null;
		Token REGISTER342=null;
		Token COMMA343=null;
		ParserRuleReturnScope field_reference344 =null;

		CommonTree INSTRUCTION_FORMAT21c_FIELD341_tree=null;
		CommonTree REGISTER342_tree=null;
		CommonTree COMMA343_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_FIELD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_FIELD");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:938:3: ( INSTRUCTION_FORMAT21c_FIELD REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD REGISTER field_reference ) )
			// smaliParser.g:939:5: INSTRUCTION_FORMAT21c_FIELD REGISTER COMMA field_reference
			{
			INSTRUCTION_FORMAT21c_FIELD341=(Token)match(input,INSTRUCTION_FORMAT21c_FIELD,FOLLOW_INSTRUCTION_FORMAT21c_FIELD_in_insn_format21c_field4609);
			stream_INSTRUCTION_FORMAT21c_FIELD.add(INSTRUCTION_FORMAT21c_FIELD341);

			REGISTER342=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_field4611);
			stream_REGISTER.add(REGISTER342);

			COMMA343=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_field4613);
			stream_COMMA.add(COMMA343);

			pushFollow(FOLLOW_field_reference_in_insn_format21c_field4615);
			field_reference344=field_reference();
			state._fsp--;

			stream_field_reference.add(field_reference344.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT21c_FIELD, REGISTER, field_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 940:5: -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD REGISTER field_reference )
			{
				// smaliParser.g:940:8: ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD REGISTER field_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_FIELD, (retval.start), "I_STATEMENT_FORMAT21c_FIELD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_FIELD.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_field_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_field"


	public static class insn_format21c_field_odex_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_field_odex"
	// smaliParser.g:942:1: insn_format21c_field_odex : INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER field_reference ) ;
	public final smaliParser.insn_format21c_field_odex_return insn_format21c_field_odex() throws RecognitionException {
		smaliParser.insn_format21c_field_odex_return retval = new smaliParser.insn_format21c_field_odex_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_FIELD_ODEX345=null;
		Token REGISTER346=null;
		Token COMMA347=null;
		ParserRuleReturnScope field_reference348 =null;

		CommonTree INSTRUCTION_FORMAT21c_FIELD_ODEX345_tree=null;
		CommonTree REGISTER346_tree=null;
		CommonTree COMMA347_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_FIELD_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_FIELD_ODEX");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:943:3: ( INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER field_reference ) )
			// smaliParser.g:944:5: INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER COMMA field_reference
			{
			INSTRUCTION_FORMAT21c_FIELD_ODEX345=(Token)match(input,INSTRUCTION_FORMAT21c_FIELD_ODEX,FOLLOW_INSTRUCTION_FORMAT21c_FIELD_ODEX_in_insn_format21c_field_odex4647);
			stream_INSTRUCTION_FORMAT21c_FIELD_ODEX.add(INSTRUCTION_FORMAT21c_FIELD_ODEX345);

			REGISTER346=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_field_odex4649);
			stream_REGISTER.add(REGISTER346);

			COMMA347=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_field_odex4651);
			stream_COMMA.add(COMMA347);

			pushFollow(FOLLOW_field_reference_in_insn_format21c_field_odex4653);
			field_reference348=field_reference();
			state._fsp--;

			stream_field_reference.add(field_reference348.getTree());

			      if (!allowOdex || opcodes.getOpcodeByName((INSTRUCTION_FORMAT21c_FIELD_ODEX345!=null?INSTRUCTION_FORMAT21c_FIELD_ODEX345.getText():null)) == null || apiLevel >= 14) {
			        throwOdexedInstructionException(input, (INSTRUCTION_FORMAT21c_FIELD_ODEX345!=null?INSTRUCTION_FORMAT21c_FIELD_ODEX345.getText():null));
			      }

			// AST REWRITE
			// elements: field_reference, INSTRUCTION_FORMAT21c_FIELD_ODEX, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 950:5: -> ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER field_reference )
			{
				// smaliParser.g:950:8: ^( I_STATEMENT_FORMAT21c_FIELD[$start, \"I_STATEMENT_FORMAT21c_FIELD\"] INSTRUCTION_FORMAT21c_FIELD_ODEX REGISTER field_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_FIELD, (retval.start), "I_STATEMENT_FORMAT21c_FIELD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_FIELD_ODEX.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_field_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_field_odex"


	public static class insn_format21c_method_handle_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_method_handle"
	// smaliParser.g:952:1: insn_format21c_method_handle : INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER COMMA method_handle_reference -> ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE[$start, \"I_STATEMENT_FORMAT21c_METHOD_HANDLE\"] INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER method_handle_reference ) ;
	public final smaliParser.insn_format21c_method_handle_return insn_format21c_method_handle() throws RecognitionException {
		smaliParser.insn_format21c_method_handle_return retval = new smaliParser.insn_format21c_method_handle_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_METHOD_HANDLE349=null;
		Token REGISTER350=null;
		Token COMMA351=null;
		ParserRuleReturnScope method_handle_reference352 =null;

		CommonTree INSTRUCTION_FORMAT21c_METHOD_HANDLE349_tree=null;
		CommonTree REGISTER350_tree=null;
		CommonTree COMMA351_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_METHOD_HANDLE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_METHOD_HANDLE");
		RewriteRuleSubtreeStream stream_method_handle_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_handle_reference");

		try {
			// smaliParser.g:953:3: ( INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER COMMA method_handle_reference -> ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE[$start, \"I_STATEMENT_FORMAT21c_METHOD_HANDLE\"] INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER method_handle_reference ) )
			// smaliParser.g:954:5: INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER COMMA method_handle_reference
			{
			INSTRUCTION_FORMAT21c_METHOD_HANDLE349=(Token)match(input,INSTRUCTION_FORMAT21c_METHOD_HANDLE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle4691);
			stream_INSTRUCTION_FORMAT21c_METHOD_HANDLE.add(INSTRUCTION_FORMAT21c_METHOD_HANDLE349);

			REGISTER350=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_method_handle4693);
			stream_REGISTER.add(REGISTER350);

			COMMA351=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_method_handle4695);
			stream_COMMA.add(COMMA351);

			pushFollow(FOLLOW_method_handle_reference_in_insn_format21c_method_handle4697);
			method_handle_reference352=method_handle_reference();
			state._fsp--;

			stream_method_handle_reference.add(method_handle_reference352.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT21c_METHOD_HANDLE, method_handle_reference, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 955:5: -> ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE[$start, \"I_STATEMENT_FORMAT21c_METHOD_HANDLE\"] INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER method_handle_reference )
			{
				// smaliParser.g:955:8: ^( I_STATEMENT_FORMAT21c_METHOD_HANDLE[$start, \"I_STATEMENT_FORMAT21c_METHOD_HANDLE\"] INSTRUCTION_FORMAT21c_METHOD_HANDLE REGISTER method_handle_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_METHOD_HANDLE, (retval.start), "I_STATEMENT_FORMAT21c_METHOD_HANDLE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_METHOD_HANDLE.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_method_handle_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_method_handle"


	public static class insn_format21c_method_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_method_type"
	// smaliParser.g:958:1: insn_format21c_method_type : INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER COMMA method_prototype -> ^( I_STATEMENT_FORMAT21c_METHOD_TYPE[$start, \"I_STATEMENT_FORMAT21c_METHOD_TYPE\"] INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER method_prototype ) ;
	public final smaliParser.insn_format21c_method_type_return insn_format21c_method_type() throws RecognitionException {
		smaliParser.insn_format21c_method_type_return retval = new smaliParser.insn_format21c_method_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_METHOD_TYPE353=null;
		Token REGISTER354=null;
		Token COMMA355=null;
		ParserRuleReturnScope method_prototype356 =null;

		CommonTree INSTRUCTION_FORMAT21c_METHOD_TYPE353_tree=null;
		CommonTree REGISTER354_tree=null;
		CommonTree COMMA355_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_METHOD_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_METHOD_TYPE");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");

		try {
			// smaliParser.g:959:5: ( INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER COMMA method_prototype -> ^( I_STATEMENT_FORMAT21c_METHOD_TYPE[$start, \"I_STATEMENT_FORMAT21c_METHOD_TYPE\"] INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER method_prototype ) )
			// smaliParser.g:960:5: INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER COMMA method_prototype
			{
			INSTRUCTION_FORMAT21c_METHOD_TYPE353=(Token)match(input,INSTRUCTION_FORMAT21c_METHOD_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type4743);
			stream_INSTRUCTION_FORMAT21c_METHOD_TYPE.add(INSTRUCTION_FORMAT21c_METHOD_TYPE353);

			REGISTER354=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_method_type4745);
			stream_REGISTER.add(REGISTER354);

			COMMA355=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_method_type4747);
			stream_COMMA.add(COMMA355);

			pushFollow(FOLLOW_method_prototype_in_insn_format21c_method_type4749);
			method_prototype356=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype356.getTree());
			// AST REWRITE
			// elements: method_prototype, REGISTER, INSTRUCTION_FORMAT21c_METHOD_TYPE
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 961:5: -> ^( I_STATEMENT_FORMAT21c_METHOD_TYPE[$start, \"I_STATEMENT_FORMAT21c_METHOD_TYPE\"] INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER method_prototype )
			{
				// smaliParser.g:961:8: ^( I_STATEMENT_FORMAT21c_METHOD_TYPE[$start, \"I_STATEMENT_FORMAT21c_METHOD_TYPE\"] INSTRUCTION_FORMAT21c_METHOD_TYPE REGISTER method_prototype )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_METHOD_TYPE, (retval.start), "I_STATEMENT_FORMAT21c_METHOD_TYPE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_METHOD_TYPE.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_method_prototype.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_method_type"


	public static class insn_format21c_string_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_string"
	// smaliParser.g:964:1: insn_format21c_string : INSTRUCTION_FORMAT21c_STRING REGISTER COMMA STRING_LITERAL -> ^( I_STATEMENT_FORMAT21c_STRING[$start, \"I_STATEMENT_FORMAT21c_STRING\"] INSTRUCTION_FORMAT21c_STRING REGISTER STRING_LITERAL ) ;
	public final smaliParser.insn_format21c_string_return insn_format21c_string() throws RecognitionException {
		smaliParser.insn_format21c_string_return retval = new smaliParser.insn_format21c_string_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_STRING357=null;
		Token REGISTER358=null;
		Token COMMA359=null;
		Token STRING_LITERAL360=null;

		CommonTree INSTRUCTION_FORMAT21c_STRING357_tree=null;
		CommonTree REGISTER358_tree=null;
		CommonTree COMMA359_tree=null;
		CommonTree STRING_LITERAL360_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_STRING=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_STRING");

		try {
			// smaliParser.g:965:3: ( INSTRUCTION_FORMAT21c_STRING REGISTER COMMA STRING_LITERAL -> ^( I_STATEMENT_FORMAT21c_STRING[$start, \"I_STATEMENT_FORMAT21c_STRING\"] INSTRUCTION_FORMAT21c_STRING REGISTER STRING_LITERAL ) )
			// smaliParser.g:966:5: INSTRUCTION_FORMAT21c_STRING REGISTER COMMA STRING_LITERAL
			{
			INSTRUCTION_FORMAT21c_STRING357=(Token)match(input,INSTRUCTION_FORMAT21c_STRING,FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_insn_format21c_string4793);
			stream_INSTRUCTION_FORMAT21c_STRING.add(INSTRUCTION_FORMAT21c_STRING357);

			REGISTER358=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_string4795);
			stream_REGISTER.add(REGISTER358);

			COMMA359=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_string4797);
			stream_COMMA.add(COMMA359);

			STRING_LITERAL360=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_insn_format21c_string4799);
			stream_STRING_LITERAL.add(STRING_LITERAL360);

			// AST REWRITE
			// elements: REGISTER, INSTRUCTION_FORMAT21c_STRING, STRING_LITERAL
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 967:5: -> ^( I_STATEMENT_FORMAT21c_STRING[$start, \"I_STATEMENT_FORMAT21c_STRING\"] INSTRUCTION_FORMAT21c_STRING REGISTER STRING_LITERAL )
			{
				// smaliParser.g:967:8: ^( I_STATEMENT_FORMAT21c_STRING[$start, \"I_STATEMENT_FORMAT21c_STRING\"] INSTRUCTION_FORMAT21c_STRING REGISTER STRING_LITERAL )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_STRING, (retval.start), "I_STATEMENT_FORMAT21c_STRING"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_STRING.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_string"


	public static class insn_format21c_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21c_type"
	// smaliParser.g:969:1: insn_format21c_type : INSTRUCTION_FORMAT21c_TYPE REGISTER COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT21c_TYPE[$start, \"I_STATEMENT_FORMAT21c\"] INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor ) ;
	public final smaliParser.insn_format21c_type_return insn_format21c_type() throws RecognitionException {
		smaliParser.insn_format21c_type_return retval = new smaliParser.insn_format21c_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21c_TYPE361=null;
		Token REGISTER362=null;
		Token COMMA363=null;
		ParserRuleReturnScope nonvoid_type_descriptor364 =null;

		CommonTree INSTRUCTION_FORMAT21c_TYPE361_tree=null;
		CommonTree REGISTER362_tree=null;
		CommonTree COMMA363_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21c_TYPE");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");

		try {
			// smaliParser.g:970:3: ( INSTRUCTION_FORMAT21c_TYPE REGISTER COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT21c_TYPE[$start, \"I_STATEMENT_FORMAT21c\"] INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor ) )
			// smaliParser.g:971:5: INSTRUCTION_FORMAT21c_TYPE REGISTER COMMA nonvoid_type_descriptor
			{
			INSTRUCTION_FORMAT21c_TYPE361=(Token)match(input,INSTRUCTION_FORMAT21c_TYPE,FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_insn_format21c_type4831);
			stream_INSTRUCTION_FORMAT21c_TYPE.add(INSTRUCTION_FORMAT21c_TYPE361);

			REGISTER362=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21c_type4833);
			stream_REGISTER.add(REGISTER362);

			COMMA363=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21c_type4835);
			stream_COMMA.add(COMMA363);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format21c_type4837);
			nonvoid_type_descriptor364=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor364.getTree());
			// AST REWRITE
			// elements: nonvoid_type_descriptor, REGISTER, INSTRUCTION_FORMAT21c_TYPE
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 972:5: -> ^( I_STATEMENT_FORMAT21c_TYPE[$start, \"I_STATEMENT_FORMAT21c\"] INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor )
			{
				// smaliParser.g:972:8: ^( I_STATEMENT_FORMAT21c_TYPE[$start, \"I_STATEMENT_FORMAT21c\"] INSTRUCTION_FORMAT21c_TYPE REGISTER nonvoid_type_descriptor )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21c_TYPE, (retval.start), "I_STATEMENT_FORMAT21c"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21c_TYPE.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21c_type"


	public static class insn_format21ih_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21ih"
	// smaliParser.g:974:1: insn_format21ih : INSTRUCTION_FORMAT21ih REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT21ih[$start, \"I_STATEMENT_FORMAT21ih\"] INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal ) ;
	public final smaliParser.insn_format21ih_return insn_format21ih() throws RecognitionException {
		smaliParser.insn_format21ih_return retval = new smaliParser.insn_format21ih_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21ih365=null;
		Token REGISTER366=null;
		Token COMMA367=null;
		ParserRuleReturnScope fixed_32bit_literal368 =null;

		CommonTree INSTRUCTION_FORMAT21ih365_tree=null;
		CommonTree REGISTER366_tree=null;
		CommonTree COMMA367_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21ih=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21ih");
		RewriteRuleSubtreeStream stream_fixed_32bit_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_32bit_literal");

		try {
			// smaliParser.g:975:3: ( INSTRUCTION_FORMAT21ih REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT21ih[$start, \"I_STATEMENT_FORMAT21ih\"] INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal ) )
			// smaliParser.g:976:5: INSTRUCTION_FORMAT21ih REGISTER COMMA fixed_32bit_literal
			{
			INSTRUCTION_FORMAT21ih365=(Token)match(input,INSTRUCTION_FORMAT21ih,FOLLOW_INSTRUCTION_FORMAT21ih_in_insn_format21ih4869);
			stream_INSTRUCTION_FORMAT21ih.add(INSTRUCTION_FORMAT21ih365);

			REGISTER366=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21ih4871);
			stream_REGISTER.add(REGISTER366);

			COMMA367=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21ih4873);
			stream_COMMA.add(COMMA367);

			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_format21ih4875);
			fixed_32bit_literal368=fixed_32bit_literal();
			state._fsp--;

			stream_fixed_32bit_literal.add(fixed_32bit_literal368.getTree());
			// AST REWRITE
			// elements: fixed_32bit_literal, INSTRUCTION_FORMAT21ih, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 977:5: -> ^( I_STATEMENT_FORMAT21ih[$start, \"I_STATEMENT_FORMAT21ih\"] INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal )
			{
				// smaliParser.g:977:8: ^( I_STATEMENT_FORMAT21ih[$start, \"I_STATEMENT_FORMAT21ih\"] INSTRUCTION_FORMAT21ih REGISTER fixed_32bit_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21ih, (retval.start), "I_STATEMENT_FORMAT21ih"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21ih.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_fixed_32bit_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21ih"


	public static class insn_format21lh_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21lh"
	// smaliParser.g:979:1: insn_format21lh : INSTRUCTION_FORMAT21lh REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT21lh[$start, \"I_STATEMENT_FORMAT21lh\"] INSTRUCTION_FORMAT21lh REGISTER fixed_32bit_literal ) ;
	public final smaliParser.insn_format21lh_return insn_format21lh() throws RecognitionException {
		smaliParser.insn_format21lh_return retval = new smaliParser.insn_format21lh_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21lh369=null;
		Token REGISTER370=null;
		Token COMMA371=null;
		ParserRuleReturnScope fixed_32bit_literal372 =null;

		CommonTree INSTRUCTION_FORMAT21lh369_tree=null;
		CommonTree REGISTER370_tree=null;
		CommonTree COMMA371_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21lh=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21lh");
		RewriteRuleSubtreeStream stream_fixed_32bit_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_32bit_literal");

		try {
			// smaliParser.g:980:3: ( INSTRUCTION_FORMAT21lh REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT21lh[$start, \"I_STATEMENT_FORMAT21lh\"] INSTRUCTION_FORMAT21lh REGISTER fixed_32bit_literal ) )
			// smaliParser.g:981:5: INSTRUCTION_FORMAT21lh REGISTER COMMA fixed_32bit_literal
			{
			INSTRUCTION_FORMAT21lh369=(Token)match(input,INSTRUCTION_FORMAT21lh,FOLLOW_INSTRUCTION_FORMAT21lh_in_insn_format21lh4907);
			stream_INSTRUCTION_FORMAT21lh.add(INSTRUCTION_FORMAT21lh369);

			REGISTER370=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21lh4909);
			stream_REGISTER.add(REGISTER370);

			COMMA371=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21lh4911);
			stream_COMMA.add(COMMA371);

			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_format21lh4913);
			fixed_32bit_literal372=fixed_32bit_literal();
			state._fsp--;

			stream_fixed_32bit_literal.add(fixed_32bit_literal372.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT21lh, REGISTER, fixed_32bit_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 982:5: -> ^( I_STATEMENT_FORMAT21lh[$start, \"I_STATEMENT_FORMAT21lh\"] INSTRUCTION_FORMAT21lh REGISTER fixed_32bit_literal )
			{
				// smaliParser.g:982:8: ^( I_STATEMENT_FORMAT21lh[$start, \"I_STATEMENT_FORMAT21lh\"] INSTRUCTION_FORMAT21lh REGISTER fixed_32bit_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21lh, (retval.start), "I_STATEMENT_FORMAT21lh"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21lh.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_fixed_32bit_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21lh"


	public static class insn_format21s_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21s"
	// smaliParser.g:984:1: insn_format21s : INSTRUCTION_FORMAT21s REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT21s[$start, \"I_STATEMENT_FORMAT21s\"] INSTRUCTION_FORMAT21s REGISTER integral_literal ) ;
	public final smaliParser.insn_format21s_return insn_format21s() throws RecognitionException {
		smaliParser.insn_format21s_return retval = new smaliParser.insn_format21s_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21s373=null;
		Token REGISTER374=null;
		Token COMMA375=null;
		ParserRuleReturnScope integral_literal376 =null;

		CommonTree INSTRUCTION_FORMAT21s373_tree=null;
		CommonTree REGISTER374_tree=null;
		CommonTree COMMA375_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21s=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21s");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:985:3: ( INSTRUCTION_FORMAT21s REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT21s[$start, \"I_STATEMENT_FORMAT21s\"] INSTRUCTION_FORMAT21s REGISTER integral_literal ) )
			// smaliParser.g:986:5: INSTRUCTION_FORMAT21s REGISTER COMMA integral_literal
			{
			INSTRUCTION_FORMAT21s373=(Token)match(input,INSTRUCTION_FORMAT21s,FOLLOW_INSTRUCTION_FORMAT21s_in_insn_format21s4945);
			stream_INSTRUCTION_FORMAT21s.add(INSTRUCTION_FORMAT21s373);

			REGISTER374=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21s4947);
			stream_REGISTER.add(REGISTER374);

			COMMA375=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21s4949);
			stream_COMMA.add(COMMA375);

			pushFollow(FOLLOW_integral_literal_in_insn_format21s4951);
			integral_literal376=integral_literal();
			state._fsp--;

			stream_integral_literal.add(integral_literal376.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT21s, REGISTER, integral_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 987:5: -> ^( I_STATEMENT_FORMAT21s[$start, \"I_STATEMENT_FORMAT21s\"] INSTRUCTION_FORMAT21s REGISTER integral_literal )
			{
				// smaliParser.g:987:8: ^( I_STATEMENT_FORMAT21s[$start, \"I_STATEMENT_FORMAT21s\"] INSTRUCTION_FORMAT21s REGISTER integral_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21s, (retval.start), "I_STATEMENT_FORMAT21s"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21s.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_integral_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21s"


	public static class insn_format21t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format21t"
	// smaliParser.g:989:1: insn_format21t : INSTRUCTION_FORMAT21t REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT21t[$start, \"I_STATEMENT_FORMAT21t\"] INSTRUCTION_FORMAT21t REGISTER label_ref ) ;
	public final smaliParser.insn_format21t_return insn_format21t() throws RecognitionException {
		smaliParser.insn_format21t_return retval = new smaliParser.insn_format21t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT21t377=null;
		Token REGISTER378=null;
		Token COMMA379=null;
		ParserRuleReturnScope label_ref380 =null;

		CommonTree INSTRUCTION_FORMAT21t377_tree=null;
		CommonTree REGISTER378_tree=null;
		CommonTree COMMA379_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT21t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT21t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:990:3: ( INSTRUCTION_FORMAT21t REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT21t[$start, \"I_STATEMENT_FORMAT21t\"] INSTRUCTION_FORMAT21t REGISTER label_ref ) )
			// smaliParser.g:991:5: INSTRUCTION_FORMAT21t REGISTER COMMA label_ref
			{
			INSTRUCTION_FORMAT21t377=(Token)match(input,INSTRUCTION_FORMAT21t,FOLLOW_INSTRUCTION_FORMAT21t_in_insn_format21t4983);
			stream_INSTRUCTION_FORMAT21t.add(INSTRUCTION_FORMAT21t377);

			REGISTER378=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format21t4985);
			stream_REGISTER.add(REGISTER378);

			COMMA379=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format21t4987);
			stream_COMMA.add(COMMA379);

			pushFollow(FOLLOW_label_ref_in_insn_format21t4989);
			label_ref380=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref380.getTree());
			// AST REWRITE
			// elements: label_ref, INSTRUCTION_FORMAT21t, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 992:5: -> ^( I_STATEMENT_FORMAT21t[$start, \"I_STATEMENT_FORMAT21t\"] INSTRUCTION_FORMAT21t REGISTER label_ref )
			{
				// smaliParser.g:992:8: ^( I_STATEMENT_FORMAT21t[$start, \"I_STATEMENT_FORMAT21t\"] INSTRUCTION_FORMAT21t REGISTER label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT21t, (retval.start), "I_STATEMENT_FORMAT21t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT21t.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format21t"


	public static class insn_format22b_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22b"
	// smaliParser.g:994:1: insn_format22b : INSTRUCTION_FORMAT22b REGISTER COMMA REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT22b[$start, \"I_STATEMENT_FORMAT22b\"] INSTRUCTION_FORMAT22b REGISTER REGISTER integral_literal ) ;
	public final smaliParser.insn_format22b_return insn_format22b() throws RecognitionException {
		smaliParser.insn_format22b_return retval = new smaliParser.insn_format22b_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22b381=null;
		Token REGISTER382=null;
		Token COMMA383=null;
		Token REGISTER384=null;
		Token COMMA385=null;
		ParserRuleReturnScope integral_literal386 =null;

		CommonTree INSTRUCTION_FORMAT22b381_tree=null;
		CommonTree REGISTER382_tree=null;
		CommonTree COMMA383_tree=null;
		CommonTree REGISTER384_tree=null;
		CommonTree COMMA385_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22b=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22b");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:995:3: ( INSTRUCTION_FORMAT22b REGISTER COMMA REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT22b[$start, \"I_STATEMENT_FORMAT22b\"] INSTRUCTION_FORMAT22b REGISTER REGISTER integral_literal ) )
			// smaliParser.g:996:5: INSTRUCTION_FORMAT22b REGISTER COMMA REGISTER COMMA integral_literal
			{
			INSTRUCTION_FORMAT22b381=(Token)match(input,INSTRUCTION_FORMAT22b,FOLLOW_INSTRUCTION_FORMAT22b_in_insn_format22b5021);
			stream_INSTRUCTION_FORMAT22b.add(INSTRUCTION_FORMAT22b381);

			REGISTER382=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22b5023);
			stream_REGISTER.add(REGISTER382);

			COMMA383=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22b5025);
			stream_COMMA.add(COMMA383);

			REGISTER384=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22b5027);
			stream_REGISTER.add(REGISTER384);

			COMMA385=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22b5029);
			stream_COMMA.add(COMMA385);

			pushFollow(FOLLOW_integral_literal_in_insn_format22b5031);
			integral_literal386=integral_literal();
			state._fsp--;

			stream_integral_literal.add(integral_literal386.getTree());
			// AST REWRITE
			// elements: integral_literal, REGISTER, REGISTER, INSTRUCTION_FORMAT22b
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 997:5: -> ^( I_STATEMENT_FORMAT22b[$start, \"I_STATEMENT_FORMAT22b\"] INSTRUCTION_FORMAT22b REGISTER REGISTER integral_literal )
			{
				// smaliParser.g:997:8: ^( I_STATEMENT_FORMAT22b[$start, \"I_STATEMENT_FORMAT22b\"] INSTRUCTION_FORMAT22b REGISTER REGISTER integral_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22b, (retval.start), "I_STATEMENT_FORMAT22b"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22b.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_integral_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22b"


	public static class insn_format22c_field_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22c_field"
	// smaliParser.g:999:1: insn_format22c_field : INSTRUCTION_FORMAT22c_FIELD REGISTER COMMA REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD REGISTER REGISTER field_reference ) ;
	public final smaliParser.insn_format22c_field_return insn_format22c_field() throws RecognitionException {
		smaliParser.insn_format22c_field_return retval = new smaliParser.insn_format22c_field_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22c_FIELD387=null;
		Token REGISTER388=null;
		Token COMMA389=null;
		Token REGISTER390=null;
		Token COMMA391=null;
		ParserRuleReturnScope field_reference392 =null;

		CommonTree INSTRUCTION_FORMAT22c_FIELD387_tree=null;
		CommonTree REGISTER388_tree=null;
		CommonTree COMMA389_tree=null;
		CommonTree REGISTER390_tree=null;
		CommonTree COMMA391_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_FIELD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_FIELD");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:1000:3: ( INSTRUCTION_FORMAT22c_FIELD REGISTER COMMA REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD REGISTER REGISTER field_reference ) )
			// smaliParser.g:1001:5: INSTRUCTION_FORMAT22c_FIELD REGISTER COMMA REGISTER COMMA field_reference
			{
			INSTRUCTION_FORMAT22c_FIELD387=(Token)match(input,INSTRUCTION_FORMAT22c_FIELD,FOLLOW_INSTRUCTION_FORMAT22c_FIELD_in_insn_format22c_field5065);
			stream_INSTRUCTION_FORMAT22c_FIELD.add(INSTRUCTION_FORMAT22c_FIELD387);

			REGISTER388=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field5067);
			stream_REGISTER.add(REGISTER388);

			COMMA389=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_field5069);
			stream_COMMA.add(COMMA389);

			REGISTER390=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field5071);
			stream_REGISTER.add(REGISTER390);

			COMMA391=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_field5073);
			stream_COMMA.add(COMMA391);

			pushFollow(FOLLOW_field_reference_in_insn_format22c_field5075);
			field_reference392=field_reference();
			state._fsp--;

			stream_field_reference.add(field_reference392.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT22c_FIELD, REGISTER, REGISTER, field_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1002:5: -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD REGISTER REGISTER field_reference )
			{
				// smaliParser.g:1002:8: ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD REGISTER REGISTER field_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22c_FIELD, (retval.start), "I_STATEMENT_FORMAT22c_FIELD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22c_FIELD.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_field_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22c_field"


	public static class insn_format22c_field_odex_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22c_field_odex"
	// smaliParser.g:1004:1: insn_format22c_field_odex : INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER COMMA REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER REGISTER field_reference ) ;
	public final smaliParser.insn_format22c_field_odex_return insn_format22c_field_odex() throws RecognitionException {
		smaliParser.insn_format22c_field_odex_return retval = new smaliParser.insn_format22c_field_odex_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22c_FIELD_ODEX393=null;
		Token REGISTER394=null;
		Token COMMA395=null;
		Token REGISTER396=null;
		Token COMMA397=null;
		ParserRuleReturnScope field_reference398 =null;

		CommonTree INSTRUCTION_FORMAT22c_FIELD_ODEX393_tree=null;
		CommonTree REGISTER394_tree=null;
		CommonTree COMMA395_tree=null;
		CommonTree REGISTER396_tree=null;
		CommonTree COMMA397_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_FIELD_ODEX=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_FIELD_ODEX");
		RewriteRuleSubtreeStream stream_field_reference=new RewriteRuleSubtreeStream(adaptor,"rule field_reference");

		try {
			// smaliParser.g:1005:3: ( INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER COMMA REGISTER COMMA field_reference -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER REGISTER field_reference ) )
			// smaliParser.g:1006:5: INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER COMMA REGISTER COMMA field_reference
			{
			INSTRUCTION_FORMAT22c_FIELD_ODEX393=(Token)match(input,INSTRUCTION_FORMAT22c_FIELD_ODEX,FOLLOW_INSTRUCTION_FORMAT22c_FIELD_ODEX_in_insn_format22c_field_odex5109);
			stream_INSTRUCTION_FORMAT22c_FIELD_ODEX.add(INSTRUCTION_FORMAT22c_FIELD_ODEX393);

			REGISTER394=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field_odex5111);
			stream_REGISTER.add(REGISTER394);

			COMMA395=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_field_odex5113);
			stream_COMMA.add(COMMA395);

			REGISTER396=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_field_odex5115);
			stream_REGISTER.add(REGISTER396);

			COMMA397=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_field_odex5117);
			stream_COMMA.add(COMMA397);

			pushFollow(FOLLOW_field_reference_in_insn_format22c_field_odex5119);
			field_reference398=field_reference();
			state._fsp--;

			stream_field_reference.add(field_reference398.getTree());

			      if (!allowOdex || opcodes.getOpcodeByName((INSTRUCTION_FORMAT22c_FIELD_ODEX393!=null?INSTRUCTION_FORMAT22c_FIELD_ODEX393.getText():null)) == null || apiLevel >= 14) {
			        throwOdexedInstructionException(input, (INSTRUCTION_FORMAT22c_FIELD_ODEX393!=null?INSTRUCTION_FORMAT22c_FIELD_ODEX393.getText():null));
			      }

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT22c_FIELD_ODEX, REGISTER, field_reference, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1012:5: -> ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER REGISTER field_reference )
			{
				// smaliParser.g:1012:8: ^( I_STATEMENT_FORMAT22c_FIELD[$start, \"I_STATEMENT_FORMAT22c_FIELD\"] INSTRUCTION_FORMAT22c_FIELD_ODEX REGISTER REGISTER field_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22c_FIELD, (retval.start), "I_STATEMENT_FORMAT22c_FIELD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22c_FIELD_ODEX.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_field_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22c_field_odex"


	public static class insn_format22c_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22c_type"
	// smaliParser.g:1014:1: insn_format22c_type : INSTRUCTION_FORMAT22c_TYPE REGISTER COMMA REGISTER COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT22c_TYPE[$start, \"I_STATEMENT_FORMAT22c_TYPE\"] INSTRUCTION_FORMAT22c_TYPE REGISTER REGISTER nonvoid_type_descriptor ) ;
	public final smaliParser.insn_format22c_type_return insn_format22c_type() throws RecognitionException {
		smaliParser.insn_format22c_type_return retval = new smaliParser.insn_format22c_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22c_TYPE399=null;
		Token REGISTER400=null;
		Token COMMA401=null;
		Token REGISTER402=null;
		Token COMMA403=null;
		ParserRuleReturnScope nonvoid_type_descriptor404 =null;

		CommonTree INSTRUCTION_FORMAT22c_TYPE399_tree=null;
		CommonTree REGISTER400_tree=null;
		CommonTree COMMA401_tree=null;
		CommonTree REGISTER402_tree=null;
		CommonTree COMMA403_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22c_TYPE");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");

		try {
			// smaliParser.g:1015:3: ( INSTRUCTION_FORMAT22c_TYPE REGISTER COMMA REGISTER COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT22c_TYPE[$start, \"I_STATEMENT_FORMAT22c_TYPE\"] INSTRUCTION_FORMAT22c_TYPE REGISTER REGISTER nonvoid_type_descriptor ) )
			// smaliParser.g:1016:5: INSTRUCTION_FORMAT22c_TYPE REGISTER COMMA REGISTER COMMA nonvoid_type_descriptor
			{
			INSTRUCTION_FORMAT22c_TYPE399=(Token)match(input,INSTRUCTION_FORMAT22c_TYPE,FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_insn_format22c_type5159);
			stream_INSTRUCTION_FORMAT22c_TYPE.add(INSTRUCTION_FORMAT22c_TYPE399);

			REGISTER400=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_type5161);
			stream_REGISTER.add(REGISTER400);

			COMMA401=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_type5163);
			stream_COMMA.add(COMMA401);

			REGISTER402=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22c_type5165);
			stream_REGISTER.add(REGISTER402);

			COMMA403=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22c_type5167);
			stream_COMMA.add(COMMA403);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format22c_type5169);
			nonvoid_type_descriptor404=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor404.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT22c_TYPE, REGISTER, nonvoid_type_descriptor, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1017:5: -> ^( I_STATEMENT_FORMAT22c_TYPE[$start, \"I_STATEMENT_FORMAT22c_TYPE\"] INSTRUCTION_FORMAT22c_TYPE REGISTER REGISTER nonvoid_type_descriptor )
			{
				// smaliParser.g:1017:8: ^( I_STATEMENT_FORMAT22c_TYPE[$start, \"I_STATEMENT_FORMAT22c_TYPE\"] INSTRUCTION_FORMAT22c_TYPE REGISTER REGISTER nonvoid_type_descriptor )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22c_TYPE, (retval.start), "I_STATEMENT_FORMAT22c_TYPE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22c_TYPE.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22c_type"


	public static class insn_format22cs_field_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22cs_field"
	// smaliParser.g:1019:1: insn_format22cs_field : INSTRUCTION_FORMAT22cs_FIELD REGISTER COMMA REGISTER COMMA FIELD_OFFSET ;
	public final smaliParser.insn_format22cs_field_return insn_format22cs_field() throws RecognitionException {
		smaliParser.insn_format22cs_field_return retval = new smaliParser.insn_format22cs_field_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22cs_FIELD405=null;
		Token REGISTER406=null;
		Token COMMA407=null;
		Token REGISTER408=null;
		Token COMMA409=null;
		Token FIELD_OFFSET410=null;

		CommonTree INSTRUCTION_FORMAT22cs_FIELD405_tree=null;
		CommonTree REGISTER406_tree=null;
		CommonTree COMMA407_tree=null;
		CommonTree REGISTER408_tree=null;
		CommonTree COMMA409_tree=null;
		CommonTree FIELD_OFFSET410_tree=null;

		try {
			// smaliParser.g:1020:3: ( INSTRUCTION_FORMAT22cs_FIELD REGISTER COMMA REGISTER COMMA FIELD_OFFSET )
			// smaliParser.g:1021:5: INSTRUCTION_FORMAT22cs_FIELD REGISTER COMMA REGISTER COMMA FIELD_OFFSET
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT22cs_FIELD405=(Token)match(input,INSTRUCTION_FORMAT22cs_FIELD,FOLLOW_INSTRUCTION_FORMAT22cs_FIELD_in_insn_format22cs_field5203);
			INSTRUCTION_FORMAT22cs_FIELD405_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT22cs_FIELD405);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT22cs_FIELD405_tree);

			REGISTER406=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22cs_field5205);
			REGISTER406_tree = (CommonTree)adaptor.create(REGISTER406);
			adaptor.addChild(root_0, REGISTER406_tree);

			COMMA407=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22cs_field5207);
			COMMA407_tree = (CommonTree)adaptor.create(COMMA407);
			adaptor.addChild(root_0, COMMA407_tree);

			REGISTER408=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22cs_field5209);
			REGISTER408_tree = (CommonTree)adaptor.create(REGISTER408);
			adaptor.addChild(root_0, REGISTER408_tree);

			COMMA409=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22cs_field5211);
			COMMA409_tree = (CommonTree)adaptor.create(COMMA409);
			adaptor.addChild(root_0, COMMA409_tree);

			FIELD_OFFSET410=(Token)match(input,FIELD_OFFSET,FOLLOW_FIELD_OFFSET_in_insn_format22cs_field5213);
			FIELD_OFFSET410_tree = (CommonTree)adaptor.create(FIELD_OFFSET410);
			adaptor.addChild(root_0, FIELD_OFFSET410_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT22cs_FIELD405!=null?INSTRUCTION_FORMAT22cs_FIELD405.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22cs_field"


	public static class insn_format22s_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22s"
	// smaliParser.g:1026:1: insn_format22s : instruction_format22s REGISTER COMMA REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT22s[$start, \"I_STATEMENT_FORMAT22s\"] instruction_format22s REGISTER REGISTER integral_literal ) ;
	public final smaliParser.insn_format22s_return insn_format22s() throws RecognitionException {
		smaliParser.insn_format22s_return retval = new smaliParser.insn_format22s_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token REGISTER412=null;
		Token COMMA413=null;
		Token REGISTER414=null;
		Token COMMA415=null;
		ParserRuleReturnScope instruction_format22s411 =null;
		ParserRuleReturnScope integral_literal416 =null;

		CommonTree REGISTER412_tree=null;
		CommonTree COMMA413_tree=null;
		CommonTree REGISTER414_tree=null;
		CommonTree COMMA415_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleSubtreeStream stream_instruction_format22s=new RewriteRuleSubtreeStream(adaptor,"rule instruction_format22s");
		RewriteRuleSubtreeStream stream_integral_literal=new RewriteRuleSubtreeStream(adaptor,"rule integral_literal");

		try {
			// smaliParser.g:1027:3: ( instruction_format22s REGISTER COMMA REGISTER COMMA integral_literal -> ^( I_STATEMENT_FORMAT22s[$start, \"I_STATEMENT_FORMAT22s\"] instruction_format22s REGISTER REGISTER integral_literal ) )
			// smaliParser.g:1028:5: instruction_format22s REGISTER COMMA REGISTER COMMA integral_literal
			{
			pushFollow(FOLLOW_instruction_format22s_in_insn_format22s5234);
			instruction_format22s411=instruction_format22s();
			state._fsp--;

			stream_instruction_format22s.add(instruction_format22s411.getTree());
			REGISTER412=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22s5236);
			stream_REGISTER.add(REGISTER412);

			COMMA413=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22s5238);
			stream_COMMA.add(COMMA413);

			REGISTER414=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22s5240);
			stream_REGISTER.add(REGISTER414);

			COMMA415=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22s5242);
			stream_COMMA.add(COMMA415);

			pushFollow(FOLLOW_integral_literal_in_insn_format22s5244);
			integral_literal416=integral_literal();
			state._fsp--;

			stream_integral_literal.add(integral_literal416.getTree());
			// AST REWRITE
			// elements: REGISTER, REGISTER, instruction_format22s, integral_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1029:5: -> ^( I_STATEMENT_FORMAT22s[$start, \"I_STATEMENT_FORMAT22s\"] instruction_format22s REGISTER REGISTER integral_literal )
			{
				// smaliParser.g:1029:8: ^( I_STATEMENT_FORMAT22s[$start, \"I_STATEMENT_FORMAT22s\"] instruction_format22s REGISTER REGISTER integral_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22s, (retval.start), "I_STATEMENT_FORMAT22s"), root_1);
				adaptor.addChild(root_1, stream_instruction_format22s.nextTree());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_integral_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22s"


	public static class insn_format22t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22t"
	// smaliParser.g:1031:1: insn_format22t : INSTRUCTION_FORMAT22t REGISTER COMMA REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT22t[$start, \"I_STATEMENT_FFORMAT22t\"] INSTRUCTION_FORMAT22t REGISTER REGISTER label_ref ) ;
	public final smaliParser.insn_format22t_return insn_format22t() throws RecognitionException {
		smaliParser.insn_format22t_return retval = new smaliParser.insn_format22t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22t417=null;
		Token REGISTER418=null;
		Token COMMA419=null;
		Token REGISTER420=null;
		Token COMMA421=null;
		ParserRuleReturnScope label_ref422 =null;

		CommonTree INSTRUCTION_FORMAT22t417_tree=null;
		CommonTree REGISTER418_tree=null;
		CommonTree COMMA419_tree=null;
		CommonTree REGISTER420_tree=null;
		CommonTree COMMA421_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:1032:3: ( INSTRUCTION_FORMAT22t REGISTER COMMA REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT22t[$start, \"I_STATEMENT_FFORMAT22t\"] INSTRUCTION_FORMAT22t REGISTER REGISTER label_ref ) )
			// smaliParser.g:1033:5: INSTRUCTION_FORMAT22t REGISTER COMMA REGISTER COMMA label_ref
			{
			INSTRUCTION_FORMAT22t417=(Token)match(input,INSTRUCTION_FORMAT22t,FOLLOW_INSTRUCTION_FORMAT22t_in_insn_format22t5278);
			stream_INSTRUCTION_FORMAT22t.add(INSTRUCTION_FORMAT22t417);

			REGISTER418=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22t5280);
			stream_REGISTER.add(REGISTER418);

			COMMA419=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22t5282);
			stream_COMMA.add(COMMA419);

			REGISTER420=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22t5284);
			stream_REGISTER.add(REGISTER420);

			COMMA421=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22t5286);
			stream_COMMA.add(COMMA421);

			pushFollow(FOLLOW_label_ref_in_insn_format22t5288);
			label_ref422=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref422.getTree());
			// AST REWRITE
			// elements: REGISTER, INSTRUCTION_FORMAT22t, label_ref, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1034:5: -> ^( I_STATEMENT_FORMAT22t[$start, \"I_STATEMENT_FFORMAT22t\"] INSTRUCTION_FORMAT22t REGISTER REGISTER label_ref )
			{
				// smaliParser.g:1034:8: ^( I_STATEMENT_FORMAT22t[$start, \"I_STATEMENT_FFORMAT22t\"] INSTRUCTION_FORMAT22t REGISTER REGISTER label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22t, (retval.start), "I_STATEMENT_FFORMAT22t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22t.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22t"


	public static class insn_format22x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format22x"
	// smaliParser.g:1036:1: insn_format22x : INSTRUCTION_FORMAT22x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT22x[$start, \"I_STATEMENT_FORMAT22x\"] INSTRUCTION_FORMAT22x REGISTER REGISTER ) ;
	public final smaliParser.insn_format22x_return insn_format22x() throws RecognitionException {
		smaliParser.insn_format22x_return retval = new smaliParser.insn_format22x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT22x423=null;
		Token REGISTER424=null;
		Token COMMA425=null;
		Token REGISTER426=null;

		CommonTree INSTRUCTION_FORMAT22x423_tree=null;
		CommonTree REGISTER424_tree=null;
		CommonTree COMMA425_tree=null;
		CommonTree REGISTER426_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT22x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT22x");

		try {
			// smaliParser.g:1037:3: ( INSTRUCTION_FORMAT22x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT22x[$start, \"I_STATEMENT_FORMAT22x\"] INSTRUCTION_FORMAT22x REGISTER REGISTER ) )
			// smaliParser.g:1038:5: INSTRUCTION_FORMAT22x REGISTER COMMA REGISTER
			{
			INSTRUCTION_FORMAT22x423=(Token)match(input,INSTRUCTION_FORMAT22x,FOLLOW_INSTRUCTION_FORMAT22x_in_insn_format22x5322);
			stream_INSTRUCTION_FORMAT22x.add(INSTRUCTION_FORMAT22x423);

			REGISTER424=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22x5324);
			stream_REGISTER.add(REGISTER424);

			COMMA425=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format22x5326);
			stream_COMMA.add(COMMA425);

			REGISTER426=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format22x5328);
			stream_REGISTER.add(REGISTER426);

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT22x, REGISTER, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1039:5: -> ^( I_STATEMENT_FORMAT22x[$start, \"I_STATEMENT_FORMAT22x\"] INSTRUCTION_FORMAT22x REGISTER REGISTER )
			{
				// smaliParser.g:1039:8: ^( I_STATEMENT_FORMAT22x[$start, \"I_STATEMENT_FORMAT22x\"] INSTRUCTION_FORMAT22x REGISTER REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT22x, (retval.start), "I_STATEMENT_FORMAT22x"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT22x.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format22x"


	public static class insn_format23x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format23x"
	// smaliParser.g:1041:1: insn_format23x : INSTRUCTION_FORMAT23x REGISTER COMMA REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT23x[$start, \"I_STATEMENT_FORMAT23x\"] INSTRUCTION_FORMAT23x REGISTER REGISTER REGISTER ) ;
	public final smaliParser.insn_format23x_return insn_format23x() throws RecognitionException {
		smaliParser.insn_format23x_return retval = new smaliParser.insn_format23x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT23x427=null;
		Token REGISTER428=null;
		Token COMMA429=null;
		Token REGISTER430=null;
		Token COMMA431=null;
		Token REGISTER432=null;

		CommonTree INSTRUCTION_FORMAT23x427_tree=null;
		CommonTree REGISTER428_tree=null;
		CommonTree COMMA429_tree=null;
		CommonTree REGISTER430_tree=null;
		CommonTree COMMA431_tree=null;
		CommonTree REGISTER432_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT23x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT23x");

		try {
			// smaliParser.g:1042:3: ( INSTRUCTION_FORMAT23x REGISTER COMMA REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT23x[$start, \"I_STATEMENT_FORMAT23x\"] INSTRUCTION_FORMAT23x REGISTER REGISTER REGISTER ) )
			// smaliParser.g:1043:5: INSTRUCTION_FORMAT23x REGISTER COMMA REGISTER COMMA REGISTER
			{
			INSTRUCTION_FORMAT23x427=(Token)match(input,INSTRUCTION_FORMAT23x,FOLLOW_INSTRUCTION_FORMAT23x_in_insn_format23x5360);
			stream_INSTRUCTION_FORMAT23x.add(INSTRUCTION_FORMAT23x427);

			REGISTER428=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x5362);
			stream_REGISTER.add(REGISTER428);

			COMMA429=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format23x5364);
			stream_COMMA.add(COMMA429);

			REGISTER430=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x5366);
			stream_REGISTER.add(REGISTER430);

			COMMA431=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format23x5368);
			stream_COMMA.add(COMMA431);

			REGISTER432=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format23x5370);
			stream_REGISTER.add(REGISTER432);

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT23x, REGISTER, REGISTER, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1044:5: -> ^( I_STATEMENT_FORMAT23x[$start, \"I_STATEMENT_FORMAT23x\"] INSTRUCTION_FORMAT23x REGISTER REGISTER REGISTER )
			{
				// smaliParser.g:1044:8: ^( I_STATEMENT_FORMAT23x[$start, \"I_STATEMENT_FORMAT23x\"] INSTRUCTION_FORMAT23x REGISTER REGISTER REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT23x, (retval.start), "I_STATEMENT_FORMAT23x"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT23x.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format23x"


	public static class insn_format30t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format30t"
	// smaliParser.g:1046:1: insn_format30t : INSTRUCTION_FORMAT30t label_ref -> ^( I_STATEMENT_FORMAT30t[$start, \"I_STATEMENT_FORMAT30t\"] INSTRUCTION_FORMAT30t label_ref ) ;
	public final smaliParser.insn_format30t_return insn_format30t() throws RecognitionException {
		smaliParser.insn_format30t_return retval = new smaliParser.insn_format30t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT30t433=null;
		ParserRuleReturnScope label_ref434 =null;

		CommonTree INSTRUCTION_FORMAT30t433_tree=null;
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT30t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT30t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:1047:3: ( INSTRUCTION_FORMAT30t label_ref -> ^( I_STATEMENT_FORMAT30t[$start, \"I_STATEMENT_FORMAT30t\"] INSTRUCTION_FORMAT30t label_ref ) )
			// smaliParser.g:1048:5: INSTRUCTION_FORMAT30t label_ref
			{
			INSTRUCTION_FORMAT30t433=(Token)match(input,INSTRUCTION_FORMAT30t,FOLLOW_INSTRUCTION_FORMAT30t_in_insn_format30t5404);
			stream_INSTRUCTION_FORMAT30t.add(INSTRUCTION_FORMAT30t433);

			pushFollow(FOLLOW_label_ref_in_insn_format30t5406);
			label_ref434=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref434.getTree());
			// AST REWRITE
			// elements: label_ref, INSTRUCTION_FORMAT30t
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1049:5: -> ^( I_STATEMENT_FORMAT30t[$start, \"I_STATEMENT_FORMAT30t\"] INSTRUCTION_FORMAT30t label_ref )
			{
				// smaliParser.g:1049:8: ^( I_STATEMENT_FORMAT30t[$start, \"I_STATEMENT_FORMAT30t\"] INSTRUCTION_FORMAT30t label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT30t, (retval.start), "I_STATEMENT_FORMAT30t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT30t.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format30t"


	public static class insn_format31c_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format31c"
	// smaliParser.g:1051:1: insn_format31c : INSTRUCTION_FORMAT31c REGISTER COMMA STRING_LITERAL -> ^( I_STATEMENT_FORMAT31c[$start, \"I_STATEMENT_FORMAT31c\"] INSTRUCTION_FORMAT31c REGISTER STRING_LITERAL ) ;
	public final smaliParser.insn_format31c_return insn_format31c() throws RecognitionException {
		smaliParser.insn_format31c_return retval = new smaliParser.insn_format31c_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT31c435=null;
		Token REGISTER436=null;
		Token COMMA437=null;
		Token STRING_LITERAL438=null;

		CommonTree INSTRUCTION_FORMAT31c435_tree=null;
		CommonTree REGISTER436_tree=null;
		CommonTree COMMA437_tree=null;
		CommonTree STRING_LITERAL438_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT31c=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT31c");
		RewriteRuleTokenStream stream_STRING_LITERAL=new RewriteRuleTokenStream(adaptor,"token STRING_LITERAL");

		try {
			// smaliParser.g:1052:3: ( INSTRUCTION_FORMAT31c REGISTER COMMA STRING_LITERAL -> ^( I_STATEMENT_FORMAT31c[$start, \"I_STATEMENT_FORMAT31c\"] INSTRUCTION_FORMAT31c REGISTER STRING_LITERAL ) )
			// smaliParser.g:1053:5: INSTRUCTION_FORMAT31c REGISTER COMMA STRING_LITERAL
			{
			INSTRUCTION_FORMAT31c435=(Token)match(input,INSTRUCTION_FORMAT31c,FOLLOW_INSTRUCTION_FORMAT31c_in_insn_format31c5436);
			stream_INSTRUCTION_FORMAT31c.add(INSTRUCTION_FORMAT31c435);

			REGISTER436=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31c5438);
			stream_REGISTER.add(REGISTER436);

			COMMA437=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format31c5440);
			stream_COMMA.add(COMMA437);

			STRING_LITERAL438=(Token)match(input,STRING_LITERAL,FOLLOW_STRING_LITERAL_in_insn_format31c5442);
			stream_STRING_LITERAL.add(STRING_LITERAL438);

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT31c, STRING_LITERAL, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1054:5: -> ^( I_STATEMENT_FORMAT31c[$start, \"I_STATEMENT_FORMAT31c\"] INSTRUCTION_FORMAT31c REGISTER STRING_LITERAL )
			{
				// smaliParser.g:1054:7: ^( I_STATEMENT_FORMAT31c[$start, \"I_STATEMENT_FORMAT31c\"] INSTRUCTION_FORMAT31c REGISTER STRING_LITERAL )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT31c, (retval.start), "I_STATEMENT_FORMAT31c"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT31c.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_STRING_LITERAL.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format31c"


	public static class insn_format31i_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format31i"
	// smaliParser.g:1056:1: insn_format31i : instruction_format31i REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT31i[$start, \"I_STATEMENT_FORMAT31i\"] instruction_format31i REGISTER fixed_32bit_literal ) ;
	public final smaliParser.insn_format31i_return insn_format31i() throws RecognitionException {
		smaliParser.insn_format31i_return retval = new smaliParser.insn_format31i_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token REGISTER440=null;
		Token COMMA441=null;
		ParserRuleReturnScope instruction_format31i439 =null;
		ParserRuleReturnScope fixed_32bit_literal442 =null;

		CommonTree REGISTER440_tree=null;
		CommonTree COMMA441_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleSubtreeStream stream_fixed_32bit_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_32bit_literal");
		RewriteRuleSubtreeStream stream_instruction_format31i=new RewriteRuleSubtreeStream(adaptor,"rule instruction_format31i");

		try {
			// smaliParser.g:1057:3: ( instruction_format31i REGISTER COMMA fixed_32bit_literal -> ^( I_STATEMENT_FORMAT31i[$start, \"I_STATEMENT_FORMAT31i\"] instruction_format31i REGISTER fixed_32bit_literal ) )
			// smaliParser.g:1058:5: instruction_format31i REGISTER COMMA fixed_32bit_literal
			{
			pushFollow(FOLLOW_instruction_format31i_in_insn_format31i5473);
			instruction_format31i439=instruction_format31i();
			state._fsp--;

			stream_instruction_format31i.add(instruction_format31i439.getTree());
			REGISTER440=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31i5475);
			stream_REGISTER.add(REGISTER440);

			COMMA441=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format31i5477);
			stream_COMMA.add(COMMA441);

			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_format31i5479);
			fixed_32bit_literal442=fixed_32bit_literal();
			state._fsp--;

			stream_fixed_32bit_literal.add(fixed_32bit_literal442.getTree());
			// AST REWRITE
			// elements: instruction_format31i, fixed_32bit_literal, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1059:5: -> ^( I_STATEMENT_FORMAT31i[$start, \"I_STATEMENT_FORMAT31i\"] instruction_format31i REGISTER fixed_32bit_literal )
			{
				// smaliParser.g:1059:8: ^( I_STATEMENT_FORMAT31i[$start, \"I_STATEMENT_FORMAT31i\"] instruction_format31i REGISTER fixed_32bit_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT31i, (retval.start), "I_STATEMENT_FORMAT31i"), root_1);
				adaptor.addChild(root_1, stream_instruction_format31i.nextTree());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_fixed_32bit_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format31i"


	public static class insn_format31t_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format31t"
	// smaliParser.g:1061:1: insn_format31t : INSTRUCTION_FORMAT31t REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT31t[$start, \"I_STATEMENT_FORMAT31t\"] INSTRUCTION_FORMAT31t REGISTER label_ref ) ;
	public final smaliParser.insn_format31t_return insn_format31t() throws RecognitionException {
		smaliParser.insn_format31t_return retval = new smaliParser.insn_format31t_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT31t443=null;
		Token REGISTER444=null;
		Token COMMA445=null;
		ParserRuleReturnScope label_ref446 =null;

		CommonTree INSTRUCTION_FORMAT31t443_tree=null;
		CommonTree REGISTER444_tree=null;
		CommonTree COMMA445_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT31t=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT31t");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:1062:3: ( INSTRUCTION_FORMAT31t REGISTER COMMA label_ref -> ^( I_STATEMENT_FORMAT31t[$start, \"I_STATEMENT_FORMAT31t\"] INSTRUCTION_FORMAT31t REGISTER label_ref ) )
			// smaliParser.g:1063:5: INSTRUCTION_FORMAT31t REGISTER COMMA label_ref
			{
			INSTRUCTION_FORMAT31t443=(Token)match(input,INSTRUCTION_FORMAT31t,FOLLOW_INSTRUCTION_FORMAT31t_in_insn_format31t5511);
			stream_INSTRUCTION_FORMAT31t.add(INSTRUCTION_FORMAT31t443);

			REGISTER444=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format31t5513);
			stream_REGISTER.add(REGISTER444);

			COMMA445=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format31t5515);
			stream_COMMA.add(COMMA445);

			pushFollow(FOLLOW_label_ref_in_insn_format31t5517);
			label_ref446=label_ref();
			state._fsp--;

			stream_label_ref.add(label_ref446.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT31t, REGISTER, label_ref
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1064:5: -> ^( I_STATEMENT_FORMAT31t[$start, \"I_STATEMENT_FORMAT31t\"] INSTRUCTION_FORMAT31t REGISTER label_ref )
			{
				// smaliParser.g:1064:8: ^( I_STATEMENT_FORMAT31t[$start, \"I_STATEMENT_FORMAT31t\"] INSTRUCTION_FORMAT31t REGISTER label_ref )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT31t, (retval.start), "I_STATEMENT_FORMAT31t"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT31t.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_label_ref.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format31t"


	public static class insn_format32x_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format32x"
	// smaliParser.g:1066:1: insn_format32x : INSTRUCTION_FORMAT32x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT32x[$start, \"I_STATEMENT_FORMAT32x\"] INSTRUCTION_FORMAT32x REGISTER REGISTER ) ;
	public final smaliParser.insn_format32x_return insn_format32x() throws RecognitionException {
		smaliParser.insn_format32x_return retval = new smaliParser.insn_format32x_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT32x447=null;
		Token REGISTER448=null;
		Token COMMA449=null;
		Token REGISTER450=null;

		CommonTree INSTRUCTION_FORMAT32x447_tree=null;
		CommonTree REGISTER448_tree=null;
		CommonTree COMMA449_tree=null;
		CommonTree REGISTER450_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT32x=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT32x");

		try {
			// smaliParser.g:1067:3: ( INSTRUCTION_FORMAT32x REGISTER COMMA REGISTER -> ^( I_STATEMENT_FORMAT32x[$start, \"I_STATEMENT_FORMAT32x\"] INSTRUCTION_FORMAT32x REGISTER REGISTER ) )
			// smaliParser.g:1068:5: INSTRUCTION_FORMAT32x REGISTER COMMA REGISTER
			{
			INSTRUCTION_FORMAT32x447=(Token)match(input,INSTRUCTION_FORMAT32x,FOLLOW_INSTRUCTION_FORMAT32x_in_insn_format32x5549);
			stream_INSTRUCTION_FORMAT32x.add(INSTRUCTION_FORMAT32x447);

			REGISTER448=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format32x5551);
			stream_REGISTER.add(REGISTER448);

			COMMA449=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format32x5553);
			stream_COMMA.add(COMMA449);

			REGISTER450=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format32x5555);
			stream_REGISTER.add(REGISTER450);

			// AST REWRITE
			// elements: INSTRUCTION_FORMAT32x, REGISTER, REGISTER
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1069:5: -> ^( I_STATEMENT_FORMAT32x[$start, \"I_STATEMENT_FORMAT32x\"] INSTRUCTION_FORMAT32x REGISTER REGISTER )
			{
				// smaliParser.g:1069:8: ^( I_STATEMENT_FORMAT32x[$start, \"I_STATEMENT_FORMAT32x\"] INSTRUCTION_FORMAT32x REGISTER REGISTER )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT32x, (retval.start), "I_STATEMENT_FORMAT32x"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT32x.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format32x"


	public static class insn_format35c_call_site_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35c_call_site"
	// smaliParser.g:1071:1: insn_format35c_call_site : INSTRUCTION_FORMAT35c_CALL_SITE OPEN_BRACE register_list CLOSE_BRACE COMMA call_site_reference -> ^( I_STATEMENT_FORMAT35c_CALL_SITE[$start, \"I_STATEMENT_FORMAT35c_CALL_SITE\"] INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference ) ;
	public final smaliParser.insn_format35c_call_site_return insn_format35c_call_site() throws RecognitionException {
		smaliParser.insn_format35c_call_site_return retval = new smaliParser.insn_format35c_call_site_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35c_CALL_SITE451=null;
		Token OPEN_BRACE452=null;
		Token CLOSE_BRACE454=null;
		Token COMMA455=null;
		ParserRuleReturnScope register_list453 =null;
		ParserRuleReturnScope call_site_reference456 =null;

		CommonTree INSTRUCTION_FORMAT35c_CALL_SITE451_tree=null;
		CommonTree OPEN_BRACE452_tree=null;
		CommonTree CLOSE_BRACE454_tree=null;
		CommonTree COMMA455_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_CALL_SITE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_CALL_SITE");
		RewriteRuleSubtreeStream stream_register_list=new RewriteRuleSubtreeStream(adaptor,"rule register_list");
		RewriteRuleSubtreeStream stream_call_site_reference=new RewriteRuleSubtreeStream(adaptor,"rule call_site_reference");

		try {
			// smaliParser.g:1072:3: ( INSTRUCTION_FORMAT35c_CALL_SITE OPEN_BRACE register_list CLOSE_BRACE COMMA call_site_reference -> ^( I_STATEMENT_FORMAT35c_CALL_SITE[$start, \"I_STATEMENT_FORMAT35c_CALL_SITE\"] INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference ) )
			// smaliParser.g:1074:5: INSTRUCTION_FORMAT35c_CALL_SITE OPEN_BRACE register_list CLOSE_BRACE COMMA call_site_reference
			{
			INSTRUCTION_FORMAT35c_CALL_SITE451=(Token)match(input,INSTRUCTION_FORMAT35c_CALL_SITE,FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_insn_format35c_call_site5592);
			stream_INSTRUCTION_FORMAT35c_CALL_SITE.add(INSTRUCTION_FORMAT35c_CALL_SITE451);

			OPEN_BRACE452=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35c_call_site5594);
			stream_OPEN_BRACE.add(OPEN_BRACE452);

			pushFollow(FOLLOW_register_list_in_insn_format35c_call_site5596);
			register_list453=register_list();
			state._fsp--;

			stream_register_list.add(register_list453.getTree());
			CLOSE_BRACE454=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35c_call_site5598);
			stream_CLOSE_BRACE.add(CLOSE_BRACE454);

			COMMA455=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35c_call_site5600);
			stream_COMMA.add(COMMA455);

			pushFollow(FOLLOW_call_site_reference_in_insn_format35c_call_site5602);
			call_site_reference456=call_site_reference();
			state._fsp--;

			stream_call_site_reference.add(call_site_reference456.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT35c_CALL_SITE, call_site_reference, register_list
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1075:5: -> ^( I_STATEMENT_FORMAT35c_CALL_SITE[$start, \"I_STATEMENT_FORMAT35c_CALL_SITE\"] INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference )
			{
				// smaliParser.g:1075:8: ^( I_STATEMENT_FORMAT35c_CALL_SITE[$start, \"I_STATEMENT_FORMAT35c_CALL_SITE\"] INSTRUCTION_FORMAT35c_CALL_SITE register_list call_site_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT35c_CALL_SITE, (retval.start), "I_STATEMENT_FORMAT35c_CALL_SITE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT35c_CALL_SITE.nextNode());
				adaptor.addChild(root_1, stream_register_list.nextTree());
				adaptor.addChild(root_1, stream_call_site_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35c_call_site"


	public static class insn_format35c_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35c_method"
	// smaliParser.g:1077:1: insn_format35c_method : instruction_format35c_method OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference -> ^( I_STATEMENT_FORMAT35c_METHOD[$start, \"I_STATEMENT_FORMAT35c_METHOD\"] instruction_format35c_method register_list method_reference ) ;
	public final smaliParser.insn_format35c_method_return insn_format35c_method() throws RecognitionException {
		smaliParser.insn_format35c_method_return retval = new smaliParser.insn_format35c_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token OPEN_BRACE458=null;
		Token CLOSE_BRACE460=null;
		Token COMMA461=null;
		ParserRuleReturnScope instruction_format35c_method457 =null;
		ParserRuleReturnScope register_list459 =null;
		ParserRuleReturnScope method_reference462 =null;

		CommonTree OPEN_BRACE458_tree=null;
		CommonTree CLOSE_BRACE460_tree=null;
		CommonTree COMMA461_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleSubtreeStream stream_instruction_format35c_method=new RewriteRuleSubtreeStream(adaptor,"rule instruction_format35c_method");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_register_list=new RewriteRuleSubtreeStream(adaptor,"rule register_list");

		try {
			// smaliParser.g:1078:3: ( instruction_format35c_method OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference -> ^( I_STATEMENT_FORMAT35c_METHOD[$start, \"I_STATEMENT_FORMAT35c_METHOD\"] instruction_format35c_method register_list method_reference ) )
			// smaliParser.g:1079:5: instruction_format35c_method OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference
			{
			pushFollow(FOLLOW_instruction_format35c_method_in_insn_format35c_method5634);
			instruction_format35c_method457=instruction_format35c_method();
			state._fsp--;

			stream_instruction_format35c_method.add(instruction_format35c_method457.getTree());
			OPEN_BRACE458=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35c_method5636);
			stream_OPEN_BRACE.add(OPEN_BRACE458);

			pushFollow(FOLLOW_register_list_in_insn_format35c_method5638);
			register_list459=register_list();
			state._fsp--;

			stream_register_list.add(register_list459.getTree());
			CLOSE_BRACE460=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35c_method5640);
			stream_CLOSE_BRACE.add(CLOSE_BRACE460);

			COMMA461=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35c_method5642);
			stream_COMMA.add(COMMA461);

			pushFollow(FOLLOW_method_reference_in_insn_format35c_method5644);
			method_reference462=method_reference();
			state._fsp--;

			stream_method_reference.add(method_reference462.getTree());
			// AST REWRITE
			// elements: register_list, instruction_format35c_method, method_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1080:5: -> ^( I_STATEMENT_FORMAT35c_METHOD[$start, \"I_STATEMENT_FORMAT35c_METHOD\"] instruction_format35c_method register_list method_reference )
			{
				// smaliParser.g:1080:8: ^( I_STATEMENT_FORMAT35c_METHOD[$start, \"I_STATEMENT_FORMAT35c_METHOD\"] instruction_format35c_method register_list method_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT35c_METHOD, (retval.start), "I_STATEMENT_FORMAT35c_METHOD"), root_1);
				adaptor.addChild(root_1, stream_instruction_format35c_method.nextTree());
				adaptor.addChild(root_1, stream_register_list.nextTree());
				adaptor.addChild(root_1, stream_method_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35c_method"


	public static class insn_format35c_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35c_type"
	// smaliParser.g:1082:1: insn_format35c_type : INSTRUCTION_FORMAT35c_TYPE OPEN_BRACE register_list CLOSE_BRACE COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT35c_TYPE[$start, \"I_STATEMENT_FORMAT35c_TYPE\"] INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor ) ;
	public final smaliParser.insn_format35c_type_return insn_format35c_type() throws RecognitionException {
		smaliParser.insn_format35c_type_return retval = new smaliParser.insn_format35c_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35c_TYPE463=null;
		Token OPEN_BRACE464=null;
		Token CLOSE_BRACE466=null;
		Token COMMA467=null;
		ParserRuleReturnScope register_list465 =null;
		ParserRuleReturnScope nonvoid_type_descriptor468 =null;

		CommonTree INSTRUCTION_FORMAT35c_TYPE463_tree=null;
		CommonTree OPEN_BRACE464_tree=null;
		CommonTree CLOSE_BRACE466_tree=null;
		CommonTree COMMA467_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT35c_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT35c_TYPE");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleSubtreeStream stream_register_list=new RewriteRuleSubtreeStream(adaptor,"rule register_list");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");

		try {
			// smaliParser.g:1083:3: ( INSTRUCTION_FORMAT35c_TYPE OPEN_BRACE register_list CLOSE_BRACE COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT35c_TYPE[$start, \"I_STATEMENT_FORMAT35c_TYPE\"] INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor ) )
			// smaliParser.g:1084:5: INSTRUCTION_FORMAT35c_TYPE OPEN_BRACE register_list CLOSE_BRACE COMMA nonvoid_type_descriptor
			{
			INSTRUCTION_FORMAT35c_TYPE463=(Token)match(input,INSTRUCTION_FORMAT35c_TYPE,FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_insn_format35c_type5676);
			stream_INSTRUCTION_FORMAT35c_TYPE.add(INSTRUCTION_FORMAT35c_TYPE463);

			OPEN_BRACE464=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35c_type5678);
			stream_OPEN_BRACE.add(OPEN_BRACE464);

			pushFollow(FOLLOW_register_list_in_insn_format35c_type5680);
			register_list465=register_list();
			state._fsp--;

			stream_register_list.add(register_list465.getTree());
			CLOSE_BRACE466=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35c_type5682);
			stream_CLOSE_BRACE.add(CLOSE_BRACE466);

			COMMA467=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35c_type5684);
			stream_COMMA.add(COMMA467);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format35c_type5686);
			nonvoid_type_descriptor468=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor468.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT35c_TYPE, nonvoid_type_descriptor, register_list
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1085:5: -> ^( I_STATEMENT_FORMAT35c_TYPE[$start, \"I_STATEMENT_FORMAT35c_TYPE\"] INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor )
			{
				// smaliParser.g:1085:8: ^( I_STATEMENT_FORMAT35c_TYPE[$start, \"I_STATEMENT_FORMAT35c_TYPE\"] INSTRUCTION_FORMAT35c_TYPE register_list nonvoid_type_descriptor )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT35c_TYPE, (retval.start), "I_STATEMENT_FORMAT35c_TYPE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT35c_TYPE.nextNode());
				adaptor.addChild(root_1, stream_register_list.nextTree());
				adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35c_type"


	public static class insn_format35c_method_odex_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35c_method_odex"
	// smaliParser.g:1087:1: insn_format35c_method_odex : INSTRUCTION_FORMAT35c_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference ;
	public final smaliParser.insn_format35c_method_odex_return insn_format35c_method_odex() throws RecognitionException {
		smaliParser.insn_format35c_method_odex_return retval = new smaliParser.insn_format35c_method_odex_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35c_METHOD_ODEX469=null;
		Token OPEN_BRACE470=null;
		Token CLOSE_BRACE472=null;
		Token COMMA473=null;
		ParserRuleReturnScope register_list471 =null;
		ParserRuleReturnScope method_reference474 =null;

		CommonTree INSTRUCTION_FORMAT35c_METHOD_ODEX469_tree=null;
		CommonTree OPEN_BRACE470_tree=null;
		CommonTree CLOSE_BRACE472_tree=null;
		CommonTree COMMA473_tree=null;

		try {
			// smaliParser.g:1088:3: ( INSTRUCTION_FORMAT35c_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference )
			// smaliParser.g:1089:5: INSTRUCTION_FORMAT35c_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT35c_METHOD_ODEX469=(Token)match(input,INSTRUCTION_FORMAT35c_METHOD_ODEX,FOLLOW_INSTRUCTION_FORMAT35c_METHOD_ODEX_in_insn_format35c_method_odex5718);
			INSTRUCTION_FORMAT35c_METHOD_ODEX469_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT35c_METHOD_ODEX469);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT35c_METHOD_ODEX469_tree);

			OPEN_BRACE470=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35c_method_odex5720);
			OPEN_BRACE470_tree = (CommonTree)adaptor.create(OPEN_BRACE470);
			adaptor.addChild(root_0, OPEN_BRACE470_tree);

			pushFollow(FOLLOW_register_list_in_insn_format35c_method_odex5722);
			register_list471=register_list();
			state._fsp--;

			adaptor.addChild(root_0, register_list471.getTree());

			CLOSE_BRACE472=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35c_method_odex5724);
			CLOSE_BRACE472_tree = (CommonTree)adaptor.create(CLOSE_BRACE472);
			adaptor.addChild(root_0, CLOSE_BRACE472_tree);

			COMMA473=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35c_method_odex5726);
			COMMA473_tree = (CommonTree)adaptor.create(COMMA473);
			adaptor.addChild(root_0, COMMA473_tree);

			pushFollow(FOLLOW_method_reference_in_insn_format35c_method_odex5728);
			method_reference474=method_reference();
			state._fsp--;

			adaptor.addChild(root_0, method_reference474.getTree());


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT35c_METHOD_ODEX469!=null?INSTRUCTION_FORMAT35c_METHOD_ODEX469.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35c_method_odex"


	public static class insn_format35mi_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35mi_method"
	// smaliParser.g:1094:1: insn_format35mi_method : INSTRUCTION_FORMAT35mi_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA INLINE_INDEX ;
	public final smaliParser.insn_format35mi_method_return insn_format35mi_method() throws RecognitionException {
		smaliParser.insn_format35mi_method_return retval = new smaliParser.insn_format35mi_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35mi_METHOD475=null;
		Token OPEN_BRACE476=null;
		Token CLOSE_BRACE478=null;
		Token COMMA479=null;
		Token INLINE_INDEX480=null;
		ParserRuleReturnScope register_list477 =null;

		CommonTree INSTRUCTION_FORMAT35mi_METHOD475_tree=null;
		CommonTree OPEN_BRACE476_tree=null;
		CommonTree CLOSE_BRACE478_tree=null;
		CommonTree COMMA479_tree=null;
		CommonTree INLINE_INDEX480_tree=null;

		try {
			// smaliParser.g:1095:3: ( INSTRUCTION_FORMAT35mi_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA INLINE_INDEX )
			// smaliParser.g:1096:5: INSTRUCTION_FORMAT35mi_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA INLINE_INDEX
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT35mi_METHOD475=(Token)match(input,INSTRUCTION_FORMAT35mi_METHOD,FOLLOW_INSTRUCTION_FORMAT35mi_METHOD_in_insn_format35mi_method5749);
			INSTRUCTION_FORMAT35mi_METHOD475_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT35mi_METHOD475);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT35mi_METHOD475_tree);

			OPEN_BRACE476=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35mi_method5751);
			OPEN_BRACE476_tree = (CommonTree)adaptor.create(OPEN_BRACE476);
			adaptor.addChild(root_0, OPEN_BRACE476_tree);

			pushFollow(FOLLOW_register_list_in_insn_format35mi_method5753);
			register_list477=register_list();
			state._fsp--;

			adaptor.addChild(root_0, register_list477.getTree());

			CLOSE_BRACE478=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35mi_method5755);
			CLOSE_BRACE478_tree = (CommonTree)adaptor.create(CLOSE_BRACE478);
			adaptor.addChild(root_0, CLOSE_BRACE478_tree);

			COMMA479=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35mi_method5757);
			COMMA479_tree = (CommonTree)adaptor.create(COMMA479);
			adaptor.addChild(root_0, COMMA479_tree);

			INLINE_INDEX480=(Token)match(input,INLINE_INDEX,FOLLOW_INLINE_INDEX_in_insn_format35mi_method5759);
			INLINE_INDEX480_tree = (CommonTree)adaptor.create(INLINE_INDEX480);
			adaptor.addChild(root_0, INLINE_INDEX480_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT35mi_METHOD475!=null?INSTRUCTION_FORMAT35mi_METHOD475.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35mi_method"


	public static class insn_format35ms_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format35ms_method"
	// smaliParser.g:1101:1: insn_format35ms_method : INSTRUCTION_FORMAT35ms_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA VTABLE_INDEX ;
	public final smaliParser.insn_format35ms_method_return insn_format35ms_method() throws RecognitionException {
		smaliParser.insn_format35ms_method_return retval = new smaliParser.insn_format35ms_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT35ms_METHOD481=null;
		Token OPEN_BRACE482=null;
		Token CLOSE_BRACE484=null;
		Token COMMA485=null;
		Token VTABLE_INDEX486=null;
		ParserRuleReturnScope register_list483 =null;

		CommonTree INSTRUCTION_FORMAT35ms_METHOD481_tree=null;
		CommonTree OPEN_BRACE482_tree=null;
		CommonTree CLOSE_BRACE484_tree=null;
		CommonTree COMMA485_tree=null;
		CommonTree VTABLE_INDEX486_tree=null;

		try {
			// smaliParser.g:1102:3: ( INSTRUCTION_FORMAT35ms_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA VTABLE_INDEX )
			// smaliParser.g:1103:5: INSTRUCTION_FORMAT35ms_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA VTABLE_INDEX
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT35ms_METHOD481=(Token)match(input,INSTRUCTION_FORMAT35ms_METHOD,FOLLOW_INSTRUCTION_FORMAT35ms_METHOD_in_insn_format35ms_method5780);
			INSTRUCTION_FORMAT35ms_METHOD481_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT35ms_METHOD481);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT35ms_METHOD481_tree);

			OPEN_BRACE482=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format35ms_method5782);
			OPEN_BRACE482_tree = (CommonTree)adaptor.create(OPEN_BRACE482);
			adaptor.addChild(root_0, OPEN_BRACE482_tree);

			pushFollow(FOLLOW_register_list_in_insn_format35ms_method5784);
			register_list483=register_list();
			state._fsp--;

			adaptor.addChild(root_0, register_list483.getTree());

			CLOSE_BRACE484=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format35ms_method5786);
			CLOSE_BRACE484_tree = (CommonTree)adaptor.create(CLOSE_BRACE484);
			adaptor.addChild(root_0, CLOSE_BRACE484_tree);

			COMMA485=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format35ms_method5788);
			COMMA485_tree = (CommonTree)adaptor.create(COMMA485);
			adaptor.addChild(root_0, COMMA485_tree);

			VTABLE_INDEX486=(Token)match(input,VTABLE_INDEX,FOLLOW_VTABLE_INDEX_in_insn_format35ms_method5790);
			VTABLE_INDEX486_tree = (CommonTree)adaptor.create(VTABLE_INDEX486);
			adaptor.addChild(root_0, VTABLE_INDEX486_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT35ms_METHOD481!=null?INSTRUCTION_FORMAT35ms_METHOD481.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format35ms_method"


	public static class insn_format3rc_call_site_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rc_call_site"
	// smaliParser.g:1108:1: insn_format3rc_call_site : INSTRUCTION_FORMAT3rc_CALL_SITE OPEN_BRACE register_range CLOSE_BRACE COMMA call_site_reference -> ^( I_STATEMENT_FORMAT3rc_CALL_SITE[$start, \"I_STATEMENT_FORMAT3rc_CALL_SITE\"] INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference ) ;
	public final smaliParser.insn_format3rc_call_site_return insn_format3rc_call_site() throws RecognitionException {
		smaliParser.insn_format3rc_call_site_return retval = new smaliParser.insn_format3rc_call_site_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rc_CALL_SITE487=null;
		Token OPEN_BRACE488=null;
		Token CLOSE_BRACE490=null;
		Token COMMA491=null;
		ParserRuleReturnScope register_range489 =null;
		ParserRuleReturnScope call_site_reference492 =null;

		CommonTree INSTRUCTION_FORMAT3rc_CALL_SITE487_tree=null;
		CommonTree OPEN_BRACE488_tree=null;
		CommonTree CLOSE_BRACE490_tree=null;
		CommonTree COMMA491_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT3rc_CALL_SITE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT3rc_CALL_SITE");
		RewriteRuleSubtreeStream stream_register_range=new RewriteRuleSubtreeStream(adaptor,"rule register_range");
		RewriteRuleSubtreeStream stream_call_site_reference=new RewriteRuleSubtreeStream(adaptor,"rule call_site_reference");

		try {
			// smaliParser.g:1109:3: ( INSTRUCTION_FORMAT3rc_CALL_SITE OPEN_BRACE register_range CLOSE_BRACE COMMA call_site_reference -> ^( I_STATEMENT_FORMAT3rc_CALL_SITE[$start, \"I_STATEMENT_FORMAT3rc_CALL_SITE\"] INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference ) )
			// smaliParser.g:1111:5: INSTRUCTION_FORMAT3rc_CALL_SITE OPEN_BRACE register_range CLOSE_BRACE COMMA call_site_reference
			{
			INSTRUCTION_FORMAT3rc_CALL_SITE487=(Token)match(input,INSTRUCTION_FORMAT3rc_CALL_SITE,FOLLOW_INSTRUCTION_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site5816);
			stream_INSTRUCTION_FORMAT3rc_CALL_SITE.add(INSTRUCTION_FORMAT3rc_CALL_SITE487);

			OPEN_BRACE488=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rc_call_site5818);
			stream_OPEN_BRACE.add(OPEN_BRACE488);

			pushFollow(FOLLOW_register_range_in_insn_format3rc_call_site5820);
			register_range489=register_range();
			state._fsp--;

			stream_register_range.add(register_range489.getTree());
			CLOSE_BRACE490=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rc_call_site5822);
			stream_CLOSE_BRACE.add(CLOSE_BRACE490);

			COMMA491=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rc_call_site5824);
			stream_COMMA.add(COMMA491);

			pushFollow(FOLLOW_call_site_reference_in_insn_format3rc_call_site5826);
			call_site_reference492=call_site_reference();
			state._fsp--;

			stream_call_site_reference.add(call_site_reference492.getTree());
			// AST REWRITE
			// elements: call_site_reference, INSTRUCTION_FORMAT3rc_CALL_SITE, register_range
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1112:5: -> ^( I_STATEMENT_FORMAT3rc_CALL_SITE[$start, \"I_STATEMENT_FORMAT3rc_CALL_SITE\"] INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference )
			{
				// smaliParser.g:1112:8: ^( I_STATEMENT_FORMAT3rc_CALL_SITE[$start, \"I_STATEMENT_FORMAT3rc_CALL_SITE\"] INSTRUCTION_FORMAT3rc_CALL_SITE register_range call_site_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT3rc_CALL_SITE, (retval.start), "I_STATEMENT_FORMAT3rc_CALL_SITE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT3rc_CALL_SITE.nextNode());
				adaptor.addChild(root_1, stream_register_range.nextTree());
				adaptor.addChild(root_1, stream_call_site_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rc_call_site"


	public static class insn_format3rc_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rc_method"
	// smaliParser.g:1114:1: insn_format3rc_method : INSTRUCTION_FORMAT3rc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference -> ^( I_STATEMENT_FORMAT3rc_METHOD[$start, \"I_STATEMENT_FORMAT3rc_METHOD\"] INSTRUCTION_FORMAT3rc_METHOD register_range method_reference ) ;
	public final smaliParser.insn_format3rc_method_return insn_format3rc_method() throws RecognitionException {
		smaliParser.insn_format3rc_method_return retval = new smaliParser.insn_format3rc_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rc_METHOD493=null;
		Token OPEN_BRACE494=null;
		Token CLOSE_BRACE496=null;
		Token COMMA497=null;
		ParserRuleReturnScope register_range495 =null;
		ParserRuleReturnScope method_reference498 =null;

		CommonTree INSTRUCTION_FORMAT3rc_METHOD493_tree=null;
		CommonTree OPEN_BRACE494_tree=null;
		CommonTree CLOSE_BRACE496_tree=null;
		CommonTree COMMA497_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT3rc_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT3rc_METHOD");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_register_range=new RewriteRuleSubtreeStream(adaptor,"rule register_range");

		try {
			// smaliParser.g:1115:3: ( INSTRUCTION_FORMAT3rc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference -> ^( I_STATEMENT_FORMAT3rc_METHOD[$start, \"I_STATEMENT_FORMAT3rc_METHOD\"] INSTRUCTION_FORMAT3rc_METHOD register_range method_reference ) )
			// smaliParser.g:1116:5: INSTRUCTION_FORMAT3rc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference
			{
			INSTRUCTION_FORMAT3rc_METHOD493=(Token)match(input,INSTRUCTION_FORMAT3rc_METHOD,FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_in_insn_format3rc_method5858);
			stream_INSTRUCTION_FORMAT3rc_METHOD.add(INSTRUCTION_FORMAT3rc_METHOD493);

			OPEN_BRACE494=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rc_method5860);
			stream_OPEN_BRACE.add(OPEN_BRACE494);

			pushFollow(FOLLOW_register_range_in_insn_format3rc_method5862);
			register_range495=register_range();
			state._fsp--;

			stream_register_range.add(register_range495.getTree());
			CLOSE_BRACE496=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rc_method5864);
			stream_CLOSE_BRACE.add(CLOSE_BRACE496);

			COMMA497=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rc_method5866);
			stream_COMMA.add(COMMA497);

			pushFollow(FOLLOW_method_reference_in_insn_format3rc_method5868);
			method_reference498=method_reference();
			state._fsp--;

			stream_method_reference.add(method_reference498.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT3rc_METHOD, register_range, method_reference
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1117:5: -> ^( I_STATEMENT_FORMAT3rc_METHOD[$start, \"I_STATEMENT_FORMAT3rc_METHOD\"] INSTRUCTION_FORMAT3rc_METHOD register_range method_reference )
			{
				// smaliParser.g:1117:8: ^( I_STATEMENT_FORMAT3rc_METHOD[$start, \"I_STATEMENT_FORMAT3rc_METHOD\"] INSTRUCTION_FORMAT3rc_METHOD register_range method_reference )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT3rc_METHOD, (retval.start), "I_STATEMENT_FORMAT3rc_METHOD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT3rc_METHOD.nextNode());
				adaptor.addChild(root_1, stream_register_range.nextTree());
				adaptor.addChild(root_1, stream_method_reference.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rc_method"


	public static class insn_format3rc_method_odex_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rc_method_odex"
	// smaliParser.g:1119:1: insn_format3rc_method_odex : INSTRUCTION_FORMAT3rc_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference ;
	public final smaliParser.insn_format3rc_method_odex_return insn_format3rc_method_odex() throws RecognitionException {
		smaliParser.insn_format3rc_method_odex_return retval = new smaliParser.insn_format3rc_method_odex_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rc_METHOD_ODEX499=null;
		Token OPEN_BRACE500=null;
		Token CLOSE_BRACE502=null;
		Token COMMA503=null;
		ParserRuleReturnScope register_list501 =null;
		ParserRuleReturnScope method_reference504 =null;

		CommonTree INSTRUCTION_FORMAT3rc_METHOD_ODEX499_tree=null;
		CommonTree OPEN_BRACE500_tree=null;
		CommonTree CLOSE_BRACE502_tree=null;
		CommonTree COMMA503_tree=null;

		try {
			// smaliParser.g:1120:3: ( INSTRUCTION_FORMAT3rc_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference )
			// smaliParser.g:1121:5: INSTRUCTION_FORMAT3rc_METHOD_ODEX OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT3rc_METHOD_ODEX499=(Token)match(input,INSTRUCTION_FORMAT3rc_METHOD_ODEX,FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_ODEX_in_insn_format3rc_method_odex5900);
			INSTRUCTION_FORMAT3rc_METHOD_ODEX499_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT3rc_METHOD_ODEX499);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT3rc_METHOD_ODEX499_tree);

			OPEN_BRACE500=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rc_method_odex5902);
			OPEN_BRACE500_tree = (CommonTree)adaptor.create(OPEN_BRACE500);
			adaptor.addChild(root_0, OPEN_BRACE500_tree);

			pushFollow(FOLLOW_register_list_in_insn_format3rc_method_odex5904);
			register_list501=register_list();
			state._fsp--;

			adaptor.addChild(root_0, register_list501.getTree());

			CLOSE_BRACE502=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rc_method_odex5906);
			CLOSE_BRACE502_tree = (CommonTree)adaptor.create(CLOSE_BRACE502);
			adaptor.addChild(root_0, CLOSE_BRACE502_tree);

			COMMA503=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rc_method_odex5908);
			COMMA503_tree = (CommonTree)adaptor.create(COMMA503);
			adaptor.addChild(root_0, COMMA503_tree);

			pushFollow(FOLLOW_method_reference_in_insn_format3rc_method_odex5910);
			method_reference504=method_reference();
			state._fsp--;

			adaptor.addChild(root_0, method_reference504.getTree());


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT3rc_METHOD_ODEX499!=null?INSTRUCTION_FORMAT3rc_METHOD_ODEX499.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rc_method_odex"


	public static class insn_format3rc_type_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rc_type"
	// smaliParser.g:1126:1: insn_format3rc_type : INSTRUCTION_FORMAT3rc_TYPE OPEN_BRACE register_range CLOSE_BRACE COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT3rc_TYPE[$start, \"I_STATEMENT_FORMAT3rc_TYPE\"] INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor ) ;
	public final smaliParser.insn_format3rc_type_return insn_format3rc_type() throws RecognitionException {
		smaliParser.insn_format3rc_type_return retval = new smaliParser.insn_format3rc_type_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rc_TYPE505=null;
		Token OPEN_BRACE506=null;
		Token CLOSE_BRACE508=null;
		Token COMMA509=null;
		ParserRuleReturnScope register_range507 =null;
		ParserRuleReturnScope nonvoid_type_descriptor510 =null;

		CommonTree INSTRUCTION_FORMAT3rc_TYPE505_tree=null;
		CommonTree OPEN_BRACE506_tree=null;
		CommonTree CLOSE_BRACE508_tree=null;
		CommonTree COMMA509_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT3rc_TYPE=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT3rc_TYPE");
		RewriteRuleSubtreeStream stream_nonvoid_type_descriptor=new RewriteRuleSubtreeStream(adaptor,"rule nonvoid_type_descriptor");
		RewriteRuleSubtreeStream stream_register_range=new RewriteRuleSubtreeStream(adaptor,"rule register_range");

		try {
			// smaliParser.g:1127:3: ( INSTRUCTION_FORMAT3rc_TYPE OPEN_BRACE register_range CLOSE_BRACE COMMA nonvoid_type_descriptor -> ^( I_STATEMENT_FORMAT3rc_TYPE[$start, \"I_STATEMENT_FORMAT3rc_TYPE\"] INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor ) )
			// smaliParser.g:1128:5: INSTRUCTION_FORMAT3rc_TYPE OPEN_BRACE register_range CLOSE_BRACE COMMA nonvoid_type_descriptor
			{
			INSTRUCTION_FORMAT3rc_TYPE505=(Token)match(input,INSTRUCTION_FORMAT3rc_TYPE,FOLLOW_INSTRUCTION_FORMAT3rc_TYPE_in_insn_format3rc_type5931);
			stream_INSTRUCTION_FORMAT3rc_TYPE.add(INSTRUCTION_FORMAT3rc_TYPE505);

			OPEN_BRACE506=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rc_type5933);
			stream_OPEN_BRACE.add(OPEN_BRACE506);

			pushFollow(FOLLOW_register_range_in_insn_format3rc_type5935);
			register_range507=register_range();
			state._fsp--;

			stream_register_range.add(register_range507.getTree());
			CLOSE_BRACE508=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rc_type5937);
			stream_CLOSE_BRACE.add(CLOSE_BRACE508);

			COMMA509=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rc_type5939);
			stream_COMMA.add(COMMA509);

			pushFollow(FOLLOW_nonvoid_type_descriptor_in_insn_format3rc_type5941);
			nonvoid_type_descriptor510=nonvoid_type_descriptor();
			state._fsp--;

			stream_nonvoid_type_descriptor.add(nonvoid_type_descriptor510.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT3rc_TYPE, register_range, nonvoid_type_descriptor
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1129:5: -> ^( I_STATEMENT_FORMAT3rc_TYPE[$start, \"I_STATEMENT_FORMAT3rc_TYPE\"] INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor )
			{
				// smaliParser.g:1129:8: ^( I_STATEMENT_FORMAT3rc_TYPE[$start, \"I_STATEMENT_FORMAT3rc_TYPE\"] INSTRUCTION_FORMAT3rc_TYPE register_range nonvoid_type_descriptor )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT3rc_TYPE, (retval.start), "I_STATEMENT_FORMAT3rc_TYPE"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT3rc_TYPE.nextNode());
				adaptor.addChild(root_1, stream_register_range.nextTree());
				adaptor.addChild(root_1, stream_nonvoid_type_descriptor.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rc_type"


	public static class insn_format3rmi_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rmi_method"
	// smaliParser.g:1131:1: insn_format3rmi_method : INSTRUCTION_FORMAT3rmi_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA INLINE_INDEX ;
	public final smaliParser.insn_format3rmi_method_return insn_format3rmi_method() throws RecognitionException {
		smaliParser.insn_format3rmi_method_return retval = new smaliParser.insn_format3rmi_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rmi_METHOD511=null;
		Token OPEN_BRACE512=null;
		Token CLOSE_BRACE514=null;
		Token COMMA515=null;
		Token INLINE_INDEX516=null;
		ParserRuleReturnScope register_range513 =null;

		CommonTree INSTRUCTION_FORMAT3rmi_METHOD511_tree=null;
		CommonTree OPEN_BRACE512_tree=null;
		CommonTree CLOSE_BRACE514_tree=null;
		CommonTree COMMA515_tree=null;
		CommonTree INLINE_INDEX516_tree=null;

		try {
			// smaliParser.g:1132:3: ( INSTRUCTION_FORMAT3rmi_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA INLINE_INDEX )
			// smaliParser.g:1133:5: INSTRUCTION_FORMAT3rmi_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA INLINE_INDEX
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT3rmi_METHOD511=(Token)match(input,INSTRUCTION_FORMAT3rmi_METHOD,FOLLOW_INSTRUCTION_FORMAT3rmi_METHOD_in_insn_format3rmi_method5973);
			INSTRUCTION_FORMAT3rmi_METHOD511_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT3rmi_METHOD511);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT3rmi_METHOD511_tree);

			OPEN_BRACE512=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rmi_method5975);
			OPEN_BRACE512_tree = (CommonTree)adaptor.create(OPEN_BRACE512);
			adaptor.addChild(root_0, OPEN_BRACE512_tree);

			pushFollow(FOLLOW_register_range_in_insn_format3rmi_method5977);
			register_range513=register_range();
			state._fsp--;

			adaptor.addChild(root_0, register_range513.getTree());

			CLOSE_BRACE514=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rmi_method5979);
			CLOSE_BRACE514_tree = (CommonTree)adaptor.create(CLOSE_BRACE514);
			adaptor.addChild(root_0, CLOSE_BRACE514_tree);

			COMMA515=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rmi_method5981);
			COMMA515_tree = (CommonTree)adaptor.create(COMMA515);
			adaptor.addChild(root_0, COMMA515_tree);

			INLINE_INDEX516=(Token)match(input,INLINE_INDEX,FOLLOW_INLINE_INDEX_in_insn_format3rmi_method5983);
			INLINE_INDEX516_tree = (CommonTree)adaptor.create(INLINE_INDEX516);
			adaptor.addChild(root_0, INLINE_INDEX516_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT3rmi_METHOD511!=null?INSTRUCTION_FORMAT3rmi_METHOD511.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rmi_method"


	public static class insn_format3rms_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format3rms_method"
	// smaliParser.g:1138:1: insn_format3rms_method : INSTRUCTION_FORMAT3rms_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA VTABLE_INDEX ;
	public final smaliParser.insn_format3rms_method_return insn_format3rms_method() throws RecognitionException {
		smaliParser.insn_format3rms_method_return retval = new smaliParser.insn_format3rms_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT3rms_METHOD517=null;
		Token OPEN_BRACE518=null;
		Token CLOSE_BRACE520=null;
		Token COMMA521=null;
		Token VTABLE_INDEX522=null;
		ParserRuleReturnScope register_range519 =null;

		CommonTree INSTRUCTION_FORMAT3rms_METHOD517_tree=null;
		CommonTree OPEN_BRACE518_tree=null;
		CommonTree CLOSE_BRACE520_tree=null;
		CommonTree COMMA521_tree=null;
		CommonTree VTABLE_INDEX522_tree=null;

		try {
			// smaliParser.g:1139:3: ( INSTRUCTION_FORMAT3rms_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA VTABLE_INDEX )
			// smaliParser.g:1140:5: INSTRUCTION_FORMAT3rms_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA VTABLE_INDEX
			{
			root_0 = (CommonTree)adaptor.nil();


			INSTRUCTION_FORMAT3rms_METHOD517=(Token)match(input,INSTRUCTION_FORMAT3rms_METHOD,FOLLOW_INSTRUCTION_FORMAT3rms_METHOD_in_insn_format3rms_method6004);
			INSTRUCTION_FORMAT3rms_METHOD517_tree = (CommonTree)adaptor.create(INSTRUCTION_FORMAT3rms_METHOD517);
			adaptor.addChild(root_0, INSTRUCTION_FORMAT3rms_METHOD517_tree);

			OPEN_BRACE518=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format3rms_method6006);
			OPEN_BRACE518_tree = (CommonTree)adaptor.create(OPEN_BRACE518);
			adaptor.addChild(root_0, OPEN_BRACE518_tree);

			pushFollow(FOLLOW_register_range_in_insn_format3rms_method6008);
			register_range519=register_range();
			state._fsp--;

			adaptor.addChild(root_0, register_range519.getTree());

			CLOSE_BRACE520=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format3rms_method6010);
			CLOSE_BRACE520_tree = (CommonTree)adaptor.create(CLOSE_BRACE520);
			adaptor.addChild(root_0, CLOSE_BRACE520_tree);

			COMMA521=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format3rms_method6012);
			COMMA521_tree = (CommonTree)adaptor.create(COMMA521);
			adaptor.addChild(root_0, COMMA521_tree);

			VTABLE_INDEX522=(Token)match(input,VTABLE_INDEX,FOLLOW_VTABLE_INDEX_in_insn_format3rms_method6014);
			VTABLE_INDEX522_tree = (CommonTree)adaptor.create(VTABLE_INDEX522);
			adaptor.addChild(root_0, VTABLE_INDEX522_tree);


			      throwOdexedInstructionException(input, (INSTRUCTION_FORMAT3rms_METHOD517!=null?INSTRUCTION_FORMAT3rms_METHOD517.getText():null));

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format3rms_method"


	public static class insn_format45cc_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format45cc_method"
	// smaliParser.g:1145:1: insn_format45cc_method : INSTRUCTION_FORMAT45cc_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference COMMA method_prototype -> ^( I_STATEMENT_FORMAT45cc_METHOD[$start, \"I_STATEMENT_FORMAT45cc_METHOD\"] INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype ) ;
	public final smaliParser.insn_format45cc_method_return insn_format45cc_method() throws RecognitionException {
		smaliParser.insn_format45cc_method_return retval = new smaliParser.insn_format45cc_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT45cc_METHOD523=null;
		Token OPEN_BRACE524=null;
		Token CLOSE_BRACE526=null;
		Token COMMA527=null;
		Token COMMA529=null;
		ParserRuleReturnScope register_list525 =null;
		ParserRuleReturnScope method_reference528 =null;
		ParserRuleReturnScope method_prototype530 =null;

		CommonTree INSTRUCTION_FORMAT45cc_METHOD523_tree=null;
		CommonTree OPEN_BRACE524_tree=null;
		CommonTree CLOSE_BRACE526_tree=null;
		CommonTree COMMA527_tree=null;
		CommonTree COMMA529_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT45cc_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT45cc_METHOD");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_register_list=new RewriteRuleSubtreeStream(adaptor,"rule register_list");

		try {
			// smaliParser.g:1146:3: ( INSTRUCTION_FORMAT45cc_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference COMMA method_prototype -> ^( I_STATEMENT_FORMAT45cc_METHOD[$start, \"I_STATEMENT_FORMAT45cc_METHOD\"] INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype ) )
			// smaliParser.g:1147:5: INSTRUCTION_FORMAT45cc_METHOD OPEN_BRACE register_list CLOSE_BRACE COMMA method_reference COMMA method_prototype
			{
			INSTRUCTION_FORMAT45cc_METHOD523=(Token)match(input,INSTRUCTION_FORMAT45cc_METHOD,FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_insn_format45cc_method6035);
			stream_INSTRUCTION_FORMAT45cc_METHOD.add(INSTRUCTION_FORMAT45cc_METHOD523);

			OPEN_BRACE524=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format45cc_method6037);
			stream_OPEN_BRACE.add(OPEN_BRACE524);

			pushFollow(FOLLOW_register_list_in_insn_format45cc_method6039);
			register_list525=register_list();
			state._fsp--;

			stream_register_list.add(register_list525.getTree());
			CLOSE_BRACE526=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format45cc_method6041);
			stream_CLOSE_BRACE.add(CLOSE_BRACE526);

			COMMA527=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format45cc_method6043);
			stream_COMMA.add(COMMA527);

			pushFollow(FOLLOW_method_reference_in_insn_format45cc_method6045);
			method_reference528=method_reference();
			state._fsp--;

			stream_method_reference.add(method_reference528.getTree());
			COMMA529=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format45cc_method6047);
			stream_COMMA.add(COMMA529);

			pushFollow(FOLLOW_method_prototype_in_insn_format45cc_method6049);
			method_prototype530=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype530.getTree());
			// AST REWRITE
			// elements: method_reference, INSTRUCTION_FORMAT45cc_METHOD, register_list, method_prototype
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1148:5: -> ^( I_STATEMENT_FORMAT45cc_METHOD[$start, \"I_STATEMENT_FORMAT45cc_METHOD\"] INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype )
			{
				// smaliParser.g:1148:8: ^( I_STATEMENT_FORMAT45cc_METHOD[$start, \"I_STATEMENT_FORMAT45cc_METHOD\"] INSTRUCTION_FORMAT45cc_METHOD register_list method_reference method_prototype )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT45cc_METHOD, (retval.start), "I_STATEMENT_FORMAT45cc_METHOD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT45cc_METHOD.nextNode());
				adaptor.addChild(root_1, stream_register_list.nextTree());
				adaptor.addChild(root_1, stream_method_reference.nextTree());
				adaptor.addChild(root_1, stream_method_prototype.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format45cc_method"


	public static class insn_format4rcc_method_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format4rcc_method"
	// smaliParser.g:1150:1: insn_format4rcc_method : INSTRUCTION_FORMAT4rcc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference COMMA method_prototype -> ^( I_STATEMENT_FORMAT4rcc_METHOD[$start, \"I_STATEMENT_FORMAT4rcc_METHOD\"] INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype ) ;
	public final smaliParser.insn_format4rcc_method_return insn_format4rcc_method() throws RecognitionException {
		smaliParser.insn_format4rcc_method_return retval = new smaliParser.insn_format4rcc_method_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT4rcc_METHOD531=null;
		Token OPEN_BRACE532=null;
		Token CLOSE_BRACE534=null;
		Token COMMA535=null;
		Token COMMA537=null;
		ParserRuleReturnScope register_range533 =null;
		ParserRuleReturnScope method_reference536 =null;
		ParserRuleReturnScope method_prototype538 =null;

		CommonTree INSTRUCTION_FORMAT4rcc_METHOD531_tree=null;
		CommonTree OPEN_BRACE532_tree=null;
		CommonTree CLOSE_BRACE534_tree=null;
		CommonTree COMMA535_tree=null;
		CommonTree COMMA537_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_OPEN_BRACE=new RewriteRuleTokenStream(adaptor,"token OPEN_BRACE");
		RewriteRuleTokenStream stream_CLOSE_BRACE=new RewriteRuleTokenStream(adaptor,"token CLOSE_BRACE");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT4rcc_METHOD=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT4rcc_METHOD");
		RewriteRuleSubtreeStream stream_method_reference=new RewriteRuleSubtreeStream(adaptor,"rule method_reference");
		RewriteRuleSubtreeStream stream_method_prototype=new RewriteRuleSubtreeStream(adaptor,"rule method_prototype");
		RewriteRuleSubtreeStream stream_register_range=new RewriteRuleSubtreeStream(adaptor,"rule register_range");

		try {
			// smaliParser.g:1151:3: ( INSTRUCTION_FORMAT4rcc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference COMMA method_prototype -> ^( I_STATEMENT_FORMAT4rcc_METHOD[$start, \"I_STATEMENT_FORMAT4rcc_METHOD\"] INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype ) )
			// smaliParser.g:1152:5: INSTRUCTION_FORMAT4rcc_METHOD OPEN_BRACE register_range CLOSE_BRACE COMMA method_reference COMMA method_prototype
			{
			INSTRUCTION_FORMAT4rcc_METHOD531=(Token)match(input,INSTRUCTION_FORMAT4rcc_METHOD,FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_insn_format4rcc_method6083);
			stream_INSTRUCTION_FORMAT4rcc_METHOD.add(INSTRUCTION_FORMAT4rcc_METHOD531);

			OPEN_BRACE532=(Token)match(input,OPEN_BRACE,FOLLOW_OPEN_BRACE_in_insn_format4rcc_method6085);
			stream_OPEN_BRACE.add(OPEN_BRACE532);

			pushFollow(FOLLOW_register_range_in_insn_format4rcc_method6087);
			register_range533=register_range();
			state._fsp--;

			stream_register_range.add(register_range533.getTree());
			CLOSE_BRACE534=(Token)match(input,CLOSE_BRACE,FOLLOW_CLOSE_BRACE_in_insn_format4rcc_method6089);
			stream_CLOSE_BRACE.add(CLOSE_BRACE534);

			COMMA535=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format4rcc_method6091);
			stream_COMMA.add(COMMA535);

			pushFollow(FOLLOW_method_reference_in_insn_format4rcc_method6093);
			method_reference536=method_reference();
			state._fsp--;

			stream_method_reference.add(method_reference536.getTree());
			COMMA537=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format4rcc_method6095);
			stream_COMMA.add(COMMA537);

			pushFollow(FOLLOW_method_prototype_in_insn_format4rcc_method6097);
			method_prototype538=method_prototype();
			state._fsp--;

			stream_method_prototype.add(method_prototype538.getTree());
			// AST REWRITE
			// elements: register_range, method_reference, method_prototype, INSTRUCTION_FORMAT4rcc_METHOD
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1153:5: -> ^( I_STATEMENT_FORMAT4rcc_METHOD[$start, \"I_STATEMENT_FORMAT4rcc_METHOD\"] INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype )
			{
				// smaliParser.g:1153:8: ^( I_STATEMENT_FORMAT4rcc_METHOD[$start, \"I_STATEMENT_FORMAT4rcc_METHOD\"] INSTRUCTION_FORMAT4rcc_METHOD register_range method_reference method_prototype )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT4rcc_METHOD, (retval.start), "I_STATEMENT_FORMAT4rcc_METHOD"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT4rcc_METHOD.nextNode());
				adaptor.addChild(root_1, stream_register_range.nextTree());
				adaptor.addChild(root_1, stream_method_reference.nextTree());
				adaptor.addChild(root_1, stream_method_prototype.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format4rcc_method"


	public static class insn_format51l_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_format51l"
	// smaliParser.g:1155:1: insn_format51l : INSTRUCTION_FORMAT51l REGISTER COMMA fixed_literal -> ^( I_STATEMENT_FORMAT51l[$start, \"I_STATEMENT_FORMAT51l\"] INSTRUCTION_FORMAT51l REGISTER fixed_literal ) ;
	public final smaliParser.insn_format51l_return insn_format51l() throws RecognitionException {
		smaliParser.insn_format51l_return retval = new smaliParser.insn_format51l_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token INSTRUCTION_FORMAT51l539=null;
		Token REGISTER540=null;
		Token COMMA541=null;
		ParserRuleReturnScope fixed_literal542 =null;

		CommonTree INSTRUCTION_FORMAT51l539_tree=null;
		CommonTree REGISTER540_tree=null;
		CommonTree COMMA541_tree=null;
		RewriteRuleTokenStream stream_COMMA=new RewriteRuleTokenStream(adaptor,"token COMMA");
		RewriteRuleTokenStream stream_REGISTER=new RewriteRuleTokenStream(adaptor,"token REGISTER");
		RewriteRuleTokenStream stream_INSTRUCTION_FORMAT51l=new RewriteRuleTokenStream(adaptor,"token INSTRUCTION_FORMAT51l");
		RewriteRuleSubtreeStream stream_fixed_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_literal");

		try {
			// smaliParser.g:1156:3: ( INSTRUCTION_FORMAT51l REGISTER COMMA fixed_literal -> ^( I_STATEMENT_FORMAT51l[$start, \"I_STATEMENT_FORMAT51l\"] INSTRUCTION_FORMAT51l REGISTER fixed_literal ) )
			// smaliParser.g:1157:5: INSTRUCTION_FORMAT51l REGISTER COMMA fixed_literal
			{
			INSTRUCTION_FORMAT51l539=(Token)match(input,INSTRUCTION_FORMAT51l,FOLLOW_INSTRUCTION_FORMAT51l_in_insn_format51l6131);
			stream_INSTRUCTION_FORMAT51l.add(INSTRUCTION_FORMAT51l539);

			REGISTER540=(Token)match(input,REGISTER,FOLLOW_REGISTER_in_insn_format51l6133);
			stream_REGISTER.add(REGISTER540);

			COMMA541=(Token)match(input,COMMA,FOLLOW_COMMA_in_insn_format51l6135);
			stream_COMMA.add(COMMA541);

			pushFollow(FOLLOW_fixed_literal_in_insn_format51l6137);
			fixed_literal542=fixed_literal();
			state._fsp--;

			stream_fixed_literal.add(fixed_literal542.getTree());
			// AST REWRITE
			// elements: INSTRUCTION_FORMAT51l, REGISTER, fixed_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1158:5: -> ^( I_STATEMENT_FORMAT51l[$start, \"I_STATEMENT_FORMAT51l\"] INSTRUCTION_FORMAT51l REGISTER fixed_literal )
			{
				// smaliParser.g:1158:8: ^( I_STATEMENT_FORMAT51l[$start, \"I_STATEMENT_FORMAT51l\"] INSTRUCTION_FORMAT51l REGISTER fixed_literal )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_FORMAT51l, (retval.start), "I_STATEMENT_FORMAT51l"), root_1);
				adaptor.addChild(root_1, stream_INSTRUCTION_FORMAT51l.nextNode());
				adaptor.addChild(root_1, stream_REGISTER.nextNode());
				adaptor.addChild(root_1, stream_fixed_literal.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_format51l"


	public static class insn_array_data_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_array_data_directive"
	// smaliParser.g:1160:1: insn_array_data_directive : ARRAY_DATA_DIRECTIVE parsed_integer_literal ( fixed_literal )* END_ARRAY_DATA_DIRECTIVE -> ^( I_STATEMENT_ARRAY_DATA[$start, \"I_STATEMENT_ARRAY_DATA\"] ^( I_ARRAY_ELEMENT_SIZE parsed_integer_literal ) ^( I_ARRAY_ELEMENTS ( fixed_literal )* ) ) ;
	public final smaliParser.insn_array_data_directive_return insn_array_data_directive() throws RecognitionException {
		smaliParser.insn_array_data_directive_return retval = new smaliParser.insn_array_data_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token ARRAY_DATA_DIRECTIVE543=null;
		Token END_ARRAY_DATA_DIRECTIVE546=null;
		ParserRuleReturnScope parsed_integer_literal544 =null;
		ParserRuleReturnScope fixed_literal545 =null;

		CommonTree ARRAY_DATA_DIRECTIVE543_tree=null;
		CommonTree END_ARRAY_DATA_DIRECTIVE546_tree=null;
		RewriteRuleTokenStream stream_END_ARRAY_DATA_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_ARRAY_DATA_DIRECTIVE");
		RewriteRuleTokenStream stream_ARRAY_DATA_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token ARRAY_DATA_DIRECTIVE");
		RewriteRuleSubtreeStream stream_parsed_integer_literal=new RewriteRuleSubtreeStream(adaptor,"rule parsed_integer_literal");
		RewriteRuleSubtreeStream stream_fixed_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_literal");

		try {
			// smaliParser.g:1161:3: ( ARRAY_DATA_DIRECTIVE parsed_integer_literal ( fixed_literal )* END_ARRAY_DATA_DIRECTIVE -> ^( I_STATEMENT_ARRAY_DATA[$start, \"I_STATEMENT_ARRAY_DATA\"] ^( I_ARRAY_ELEMENT_SIZE parsed_integer_literal ) ^( I_ARRAY_ELEMENTS ( fixed_literal )* ) ) )
			// smaliParser.g:1161:5: ARRAY_DATA_DIRECTIVE parsed_integer_literal ( fixed_literal )* END_ARRAY_DATA_DIRECTIVE
			{
			ARRAY_DATA_DIRECTIVE543=(Token)match(input,ARRAY_DATA_DIRECTIVE,FOLLOW_ARRAY_DATA_DIRECTIVE_in_insn_array_data_directive6164);
			stream_ARRAY_DATA_DIRECTIVE.add(ARRAY_DATA_DIRECTIVE543);

			pushFollow(FOLLOW_parsed_integer_literal_in_insn_array_data_directive6170);
			parsed_integer_literal544=parsed_integer_literal();
			state._fsp--;

			stream_parsed_integer_literal.add(parsed_integer_literal544.getTree());

			        int elementWidth = (parsed_integer_literal544!=null?((smaliParser.parsed_integer_literal_return)parsed_integer_literal544).value:0);

			        if (elementWidth != 0 && elementWidth != 4 && elementWidth != 8 && elementWidth != 1 && elementWidth != 2) {
			            throw new SemanticException(input, (retval.start), "Invalid element width: %d. Must be 1, 2, 4 or 8", elementWidth);
			        }

			// smaliParser.g:1169:5: ( fixed_literal )*
			loop55:
			while (true) {
				int alt55=2;
				int LA55_0 = input.LA(1);
				if ( ((LA55_0 >= BOOL_LITERAL && LA55_0 <= BYTE_LITERAL)||LA55_0==CHAR_LITERAL||(LA55_0 >= DOUBLE_LITERAL && LA55_0 <= DOUBLE_LITERAL_OR_ID)||(LA55_0 >= FLOAT_LITERAL && LA55_0 <= FLOAT_LITERAL_OR_ID)||LA55_0==LONG_LITERAL||LA55_0==NEGATIVE_INTEGER_LITERAL||LA55_0==POSITIVE_INTEGER_LITERAL||LA55_0==SHORT_LITERAL) ) {
					alt55=1;
				}

				switch (alt55) {
				case 1 :
					// smaliParser.g:1169:5: fixed_literal
					{
					pushFollow(FOLLOW_fixed_literal_in_insn_array_data_directive6182);
					fixed_literal545=fixed_literal();
					state._fsp--;

					stream_fixed_literal.add(fixed_literal545.getTree());
					}
					break;

				default :
					break loop55;
				}
			}

			END_ARRAY_DATA_DIRECTIVE546=(Token)match(input,END_ARRAY_DATA_DIRECTIVE,FOLLOW_END_ARRAY_DATA_DIRECTIVE_in_insn_array_data_directive6185);
			stream_END_ARRAY_DATA_DIRECTIVE.add(END_ARRAY_DATA_DIRECTIVE546);

			// AST REWRITE
			// elements: fixed_literal, parsed_integer_literal
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1171:5: -> ^( I_STATEMENT_ARRAY_DATA[$start, \"I_STATEMENT_ARRAY_DATA\"] ^( I_ARRAY_ELEMENT_SIZE parsed_integer_literal ) ^( I_ARRAY_ELEMENTS ( fixed_literal )* ) )
			{
				// smaliParser.g:1171:8: ^( I_STATEMENT_ARRAY_DATA[$start, \"I_STATEMENT_ARRAY_DATA\"] ^( I_ARRAY_ELEMENT_SIZE parsed_integer_literal ) ^( I_ARRAY_ELEMENTS ( fixed_literal )* ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_ARRAY_DATA, (retval.start), "I_STATEMENT_ARRAY_DATA"), root_1);
				// smaliParser.g:1171:67: ^( I_ARRAY_ELEMENT_SIZE parsed_integer_literal )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ARRAY_ELEMENT_SIZE, "I_ARRAY_ELEMENT_SIZE"), root_2);
				adaptor.addChild(root_2, stream_parsed_integer_literal.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				// smaliParser.g:1172:8: ^( I_ARRAY_ELEMENTS ( fixed_literal )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_ARRAY_ELEMENTS, "I_ARRAY_ELEMENTS"), root_2);
				// smaliParser.g:1172:27: ( fixed_literal )*
				while ( stream_fixed_literal.hasNext() ) {
					adaptor.addChild(root_2, stream_fixed_literal.nextTree());
				}
				stream_fixed_literal.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_array_data_directive"


	public static class insn_packed_switch_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_packed_switch_directive"
	// smaliParser.g:1174:1: insn_packed_switch_directive : PACKED_SWITCH_DIRECTIVE fixed_32bit_literal ( label_ref )* END_PACKED_SWITCH_DIRECTIVE -> ^( I_STATEMENT_PACKED_SWITCH[$start, \"I_STATEMENT_PACKED_SWITCH\"] ^( I_PACKED_SWITCH_START_KEY[$start, \"I_PACKED_SWITCH_START_KEY\"] fixed_32bit_literal ) ^( I_PACKED_SWITCH_ELEMENTS[$start, \"I_PACKED_SWITCH_ELEMENTS\"] ( label_ref )* ) ) ;
	public final smaliParser.insn_packed_switch_directive_return insn_packed_switch_directive() throws RecognitionException {
		smaliParser.insn_packed_switch_directive_return retval = new smaliParser.insn_packed_switch_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token PACKED_SWITCH_DIRECTIVE547=null;
		Token END_PACKED_SWITCH_DIRECTIVE550=null;
		ParserRuleReturnScope fixed_32bit_literal548 =null;
		ParserRuleReturnScope label_ref549 =null;

		CommonTree PACKED_SWITCH_DIRECTIVE547_tree=null;
		CommonTree END_PACKED_SWITCH_DIRECTIVE550_tree=null;
		RewriteRuleTokenStream stream_END_PACKED_SWITCH_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_PACKED_SWITCH_DIRECTIVE");
		RewriteRuleTokenStream stream_PACKED_SWITCH_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token PACKED_SWITCH_DIRECTIVE");
		RewriteRuleSubtreeStream stream_fixed_32bit_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_32bit_literal");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:1175:5: ( PACKED_SWITCH_DIRECTIVE fixed_32bit_literal ( label_ref )* END_PACKED_SWITCH_DIRECTIVE -> ^( I_STATEMENT_PACKED_SWITCH[$start, \"I_STATEMENT_PACKED_SWITCH\"] ^( I_PACKED_SWITCH_START_KEY[$start, \"I_PACKED_SWITCH_START_KEY\"] fixed_32bit_literal ) ^( I_PACKED_SWITCH_ELEMENTS[$start, \"I_PACKED_SWITCH_ELEMENTS\"] ( label_ref )* ) ) )
			// smaliParser.g:1175:9: PACKED_SWITCH_DIRECTIVE fixed_32bit_literal ( label_ref )* END_PACKED_SWITCH_DIRECTIVE
			{
			PACKED_SWITCH_DIRECTIVE547=(Token)match(input,PACKED_SWITCH_DIRECTIVE,FOLLOW_PACKED_SWITCH_DIRECTIVE_in_insn_packed_switch_directive6231);
			stream_PACKED_SWITCH_DIRECTIVE.add(PACKED_SWITCH_DIRECTIVE547);

			pushFollow(FOLLOW_fixed_32bit_literal_in_insn_packed_switch_directive6237);
			fixed_32bit_literal548=fixed_32bit_literal();
			state._fsp--;

			stream_fixed_32bit_literal.add(fixed_32bit_literal548.getTree());
			// smaliParser.g:1177:5: ( label_ref )*
			loop56:
			while (true) {
				int alt56=2;
				int LA56_0 = input.LA(1);
				if ( (LA56_0==COLON) ) {
					alt56=1;
				}

				switch (alt56) {
				case 1 :
					// smaliParser.g:1177:5: label_ref
					{
					pushFollow(FOLLOW_label_ref_in_insn_packed_switch_directive6243);
					label_ref549=label_ref();
					state._fsp--;

					stream_label_ref.add(label_ref549.getTree());
					}
					break;

				default :
					break loop56;
				}
			}

			END_PACKED_SWITCH_DIRECTIVE550=(Token)match(input,END_PACKED_SWITCH_DIRECTIVE,FOLLOW_END_PACKED_SWITCH_DIRECTIVE_in_insn_packed_switch_directive6250);
			stream_END_PACKED_SWITCH_DIRECTIVE.add(END_PACKED_SWITCH_DIRECTIVE550);

			// AST REWRITE
			// elements: fixed_32bit_literal, label_ref
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1179:5: -> ^( I_STATEMENT_PACKED_SWITCH[$start, \"I_STATEMENT_PACKED_SWITCH\"] ^( I_PACKED_SWITCH_START_KEY[$start, \"I_PACKED_SWITCH_START_KEY\"] fixed_32bit_literal ) ^( I_PACKED_SWITCH_ELEMENTS[$start, \"I_PACKED_SWITCH_ELEMENTS\"] ( label_ref )* ) )
			{
				// smaliParser.g:1179:8: ^( I_STATEMENT_PACKED_SWITCH[$start, \"I_STATEMENT_PACKED_SWITCH\"] ^( I_PACKED_SWITCH_START_KEY[$start, \"I_PACKED_SWITCH_START_KEY\"] fixed_32bit_literal ) ^( I_PACKED_SWITCH_ELEMENTS[$start, \"I_PACKED_SWITCH_ELEMENTS\"] ( label_ref )* ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_PACKED_SWITCH, (retval.start), "I_STATEMENT_PACKED_SWITCH"), root_1);
				// smaliParser.g:1180:10: ^( I_PACKED_SWITCH_START_KEY[$start, \"I_PACKED_SWITCH_START_KEY\"] fixed_32bit_literal )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PACKED_SWITCH_START_KEY, (retval.start), "I_PACKED_SWITCH_START_KEY"), root_2);
				adaptor.addChild(root_2, stream_fixed_32bit_literal.nextTree());
				adaptor.addChild(root_1, root_2);
				}

				// smaliParser.g:1181:10: ^( I_PACKED_SWITCH_ELEMENTS[$start, \"I_PACKED_SWITCH_ELEMENTS\"] ( label_ref )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_PACKED_SWITCH_ELEMENTS, (retval.start), "I_PACKED_SWITCH_ELEMENTS"), root_2);
				// smaliParser.g:1182:11: ( label_ref )*
				while ( stream_label_ref.hasNext() ) {
					adaptor.addChild(root_2, stream_label_ref.nextTree());
				}
				stream_label_ref.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_packed_switch_directive"


	public static class insn_sparse_switch_directive_return extends ParserRuleReturnScope {
		CommonTree tree;
		@Override
		public CommonTree getTree() { return tree; }
	};


	// $ANTLR start "insn_sparse_switch_directive"
	// smaliParser.g:1185:1: insn_sparse_switch_directive : SPARSE_SWITCH_DIRECTIVE ( fixed_32bit_literal ARROW label_ref )* END_SPARSE_SWITCH_DIRECTIVE -> ^( I_STATEMENT_SPARSE_SWITCH[$start, \"I_STATEMENT_SPARSE_SWITCH\"] ^( I_SPARSE_SWITCH_ELEMENTS[$start, \"I_SPARSE_SWITCH_ELEMENTS\"] ( fixed_32bit_literal label_ref )* ) ) ;
	public final smaliParser.insn_sparse_switch_directive_return insn_sparse_switch_directive() throws RecognitionException {
		smaliParser.insn_sparse_switch_directive_return retval = new smaliParser.insn_sparse_switch_directive_return();
		retval.start = input.LT(1);

		CommonTree root_0 = null;

		Token SPARSE_SWITCH_DIRECTIVE551=null;
		Token ARROW553=null;
		Token END_SPARSE_SWITCH_DIRECTIVE555=null;
		ParserRuleReturnScope fixed_32bit_literal552 =null;
		ParserRuleReturnScope label_ref554 =null;

		CommonTree SPARSE_SWITCH_DIRECTIVE551_tree=null;
		CommonTree ARROW553_tree=null;
		CommonTree END_SPARSE_SWITCH_DIRECTIVE555_tree=null;
		RewriteRuleTokenStream stream_ARROW=new RewriteRuleTokenStream(adaptor,"token ARROW");
		RewriteRuleTokenStream stream_SPARSE_SWITCH_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token SPARSE_SWITCH_DIRECTIVE");
		RewriteRuleTokenStream stream_END_SPARSE_SWITCH_DIRECTIVE=new RewriteRuleTokenStream(adaptor,"token END_SPARSE_SWITCH_DIRECTIVE");
		RewriteRuleSubtreeStream stream_fixed_32bit_literal=new RewriteRuleSubtreeStream(adaptor,"rule fixed_32bit_literal");
		RewriteRuleSubtreeStream stream_label_ref=new RewriteRuleSubtreeStream(adaptor,"rule label_ref");

		try {
			// smaliParser.g:1186:3: ( SPARSE_SWITCH_DIRECTIVE ( fixed_32bit_literal ARROW label_ref )* END_SPARSE_SWITCH_DIRECTIVE -> ^( I_STATEMENT_SPARSE_SWITCH[$start, \"I_STATEMENT_SPARSE_SWITCH\"] ^( I_SPARSE_SWITCH_ELEMENTS[$start, \"I_SPARSE_SWITCH_ELEMENTS\"] ( fixed_32bit_literal label_ref )* ) ) )
			// smaliParser.g:1186:7: SPARSE_SWITCH_DIRECTIVE ( fixed_32bit_literal ARROW label_ref )* END_SPARSE_SWITCH_DIRECTIVE
			{
			SPARSE_SWITCH_DIRECTIVE551=(Token)match(input,SPARSE_SWITCH_DIRECTIVE,FOLLOW_SPARSE_SWITCH_DIRECTIVE_in_insn_sparse_switch_directive6324);
			stream_SPARSE_SWITCH_DIRECTIVE.add(SPARSE_SWITCH_DIRECTIVE551);

			// smaliParser.g:1187:5: ( fixed_32bit_literal ARROW label_ref )*
			loop57:
			while (true) {
				int alt57=2;
				int LA57_0 = input.LA(1);
				if ( ((LA57_0 >= BOOL_LITERAL && LA57_0 <= BYTE_LITERAL)||LA57_0==CHAR_LITERAL||(LA57_0 >= FLOAT_LITERAL && LA57_0 <= FLOAT_LITERAL_OR_ID)||LA57_0==LONG_LITERAL||LA57_0==NEGATIVE_INTEGER_LITERAL||LA57_0==POSITIVE_INTEGER_LITERAL||LA57_0==SHORT_LITERAL) ) {
					alt57=1;
				}

				switch (alt57) {
				case 1 :
					// smaliParser.g:1187:6: fixed_32bit_literal ARROW label_ref
					{
					pushFollow(FOLLOW_fixed_32bit_literal_in_insn_sparse_switch_directive6331);
					fixed_32bit_literal552=fixed_32bit_literal();
					state._fsp--;

					stream_fixed_32bit_literal.add(fixed_32bit_literal552.getTree());
					ARROW553=(Token)match(input,ARROW,FOLLOW_ARROW_in_insn_sparse_switch_directive6333);
					stream_ARROW.add(ARROW553);

					pushFollow(FOLLOW_label_ref_in_insn_sparse_switch_directive6335);
					label_ref554=label_ref();
					state._fsp--;

					stream_label_ref.add(label_ref554.getTree());
					}
					break;

				default :
					break loop57;
				}
			}

			END_SPARSE_SWITCH_DIRECTIVE555=(Token)match(input,END_SPARSE_SWITCH_DIRECTIVE,FOLLOW_END_SPARSE_SWITCH_DIRECTIVE_in_insn_sparse_switch_directive6343);
			stream_END_SPARSE_SWITCH_DIRECTIVE.add(END_SPARSE_SWITCH_DIRECTIVE555);

			// AST REWRITE
			// elements: fixed_32bit_literal, label_ref
			// token labels:
			// rule labels: retval
			// token list labels:
			// rule list labels:
			// wildcard labels:
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (CommonTree)adaptor.nil();
			// 1189:5: -> ^( I_STATEMENT_SPARSE_SWITCH[$start, \"I_STATEMENT_SPARSE_SWITCH\"] ^( I_SPARSE_SWITCH_ELEMENTS[$start, \"I_SPARSE_SWITCH_ELEMENTS\"] ( fixed_32bit_literal label_ref )* ) )
			{
				// smaliParser.g:1189:8: ^( I_STATEMENT_SPARSE_SWITCH[$start, \"I_STATEMENT_SPARSE_SWITCH\"] ^( I_SPARSE_SWITCH_ELEMENTS[$start, \"I_SPARSE_SWITCH_ELEMENTS\"] ( fixed_32bit_literal label_ref )* ) )
				{
				CommonTree root_1 = (CommonTree)adaptor.nil();
				root_1 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_STATEMENT_SPARSE_SWITCH, (retval.start), "I_STATEMENT_SPARSE_SWITCH"), root_1);
				// smaliParser.g:1190:8: ^( I_SPARSE_SWITCH_ELEMENTS[$start, \"I_SPARSE_SWITCH_ELEMENTS\"] ( fixed_32bit_literal label_ref )* )
				{
				CommonTree root_2 = (CommonTree)adaptor.nil();
				root_2 = (CommonTree)adaptor.becomeRoot((CommonTree)adaptor.create(I_SPARSE_SWITCH_ELEMENTS, (retval.start), "I_SPARSE_SWITCH_ELEMENTS"), root_2);
				// smaliParser.g:1190:71: ( fixed_32bit_literal label_ref )*
				while ( stream_fixed_32bit_literal.hasNext()||stream_label_ref.hasNext() ) {
					adaptor.addChild(root_2, stream_fixed_32bit_literal.nextTree());
					adaptor.addChild(root_2, stream_label_ref.nextTree());
				}
				stream_fixed_32bit_literal.reset();
				stream_label_ref.reset();

				adaptor.addChild(root_1, root_2);
				}

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (CommonTree)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (CommonTree)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "insn_sparse_switch_directive"

	// Delegated rules


	protected DFA30 dfa30 = new DFA30(this);
	protected DFA40 dfa40 = new DFA40(this);
	protected DFA42 dfa42 = new DFA42(this);
	static final String DFA30_eotS =
		"\63\uffff";
	static final String DFA30_eofS =
		"\63\uffff";
	static final String DFA30_minS =
		"\1\4\60\24\2\uffff";
	static final String DFA30_maxS =
		"\1\u00d5\12\u00c3\1\u00c6\45\u00c3\2\uffff";
	static final String DFA30_acceptS =
		"\61\uffff\1\1\1\2";
	static final String DFA30_specialS =
		"\63\uffff}>";
	static final String[] DFA30_transitionS = {
			"\1\2\1\uffff\1\16\4\uffff\1\10\14\uffff\1\7\17\uffff\1\6\2\uffff\1\21"+
			"\1\22\1\23\1\uffff\1\24\1\uffff\1\25\2\uffff\1\26\1\27\1\30\1\31\1\32"+
			"\1\33\3\uffff\1\34\1\uffff\1\35\1\36\1\37\1\40\1\uffff\1\41\1\42\1\uffff"+
			"\1\43\3\uffff\1\44\1\45\1\uffff\1\46\1\47\1\50\1\51\1\52\1\53\1\54\6"+
			"\uffff\1\55\1\56\1\57\136\uffff\1\60\1\uffff\1\17\1\20\1\5\1\11\4\uffff"+
			"\1\13\1\4\1\14\1\uffff\1\12\3\uffff\1\1\5\uffff\1\3\1\15",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62\2\uffff\1\13",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"\1\61\u00ae\uffff\1\62",
			"",
			""
	};

	static final short[] DFA30_eot = DFA.unpackEncodedString(DFA30_eotS);
	static final short[] DFA30_eof = DFA.unpackEncodedString(DFA30_eofS);
	static final char[] DFA30_min = DFA.unpackEncodedStringToUnsignedChars(DFA30_minS);
	static final char[] DFA30_max = DFA.unpackEncodedStringToUnsignedChars(DFA30_maxS);
	static final short[] DFA30_accept = DFA.unpackEncodedString(DFA30_acceptS);
	static final short[] DFA30_special = DFA.unpackEncodedString(DFA30_specialS);
	static final short[][] DFA30_transition;

	static {
		int numStates = DFA30_transitionS.length;
		DFA30_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA30_transition[i] = DFA.unpackEncodedString(DFA30_transitionS[i]);
		}
	}

	protected class DFA30 extends DFA {

		public DFA30(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 30;
			this.eot = DFA30_eot;
			this.eof = DFA30_eof;
			this.min = DFA30_min;
			this.max = DFA30_max;
			this.accept = DFA30_accept;
			this.special = DFA30_special;
			this.transition = DFA30_transition;
		}
		@Override
		public String getDescription() {
			return "715:7: ( member_name COLON nonvoid_type_descriptor -> ^( I_ENCODED_FIELD ( reference_type_descriptor )? member_name nonvoid_type_descriptor ) | member_name method_prototype -> ^( I_ENCODED_METHOD ( reference_type_descriptor )? member_name method_prototype ) )";
		}
	}

	static final String DFA40_eotS =
		"\70\uffff";
	static final String DFA40_eofS =
		"\70\uffff";
	static final String DFA40_minS =
		"\1\4\1\5\1\20\60\24\1\uffff\1\4\1\11\2\uffff";
	static final String DFA40_maxS =
		"\1\u00d5\1\u00d0\1\u00c8\12\u00c3\1\u00c6\45\u00c3\1\uffff\1\u00d5\1\11"+
		"\2\uffff";
	static final String DFA40_acceptS =
		"\63\uffff\1\1\2\uffff\1\2\1\3";
	static final String DFA40_specialS =
		"\70\uffff}>";
	static final String[] DFA40_transitionS = {
			"\1\4\1\uffff\1\20\1\uffff\1\2\2\uffff\1\12\4\uffff\1\1\7\uffff\1\11\17"+
			"\uffff\1\10\2\uffff\1\23\1\24\1\25\1\uffff\1\26\1\uffff\1\27\2\uffff"+
			"\1\30\1\31\1\32\1\33\1\34\1\35\3\uffff\1\36\1\uffff\1\37\1\40\1\41\1"+
			"\42\1\uffff\1\43\1\44\1\uffff\1\45\3\uffff\1\46\1\47\1\uffff\1\50\1\51"+
			"\1\52\1\53\1\54\1\55\1\56\6\uffff\1\57\1\60\1\61\136\uffff\1\62\1\uffff"+
			"\1\21\1\22\1\7\1\13\4\uffff\1\15\1\6\1\16\1\uffff\1\14\3\uffff\1\3\5"+
			"\uffff\1\5\1\17",
			"\1\63\1\uffff\1\63\1\uffff\1\64\3\uffff\2\63\5\uffff\1\63\7\uffff\2"+
			"\63\5\uffff\1\63\7\uffff\63\63\132\uffff\3\63\11\uffff\2\63\3\uffff\1"+
			"\63\1\uffff\2\63\2\uffff\2\63",
			"\1\65\u00b7\uffff\1\65",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67\2\uffff\1\15",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"\1\66\u00ae\uffff\1\67",
			"",
			"\1\4\1\uffff\1\20\4\uffff\1\12\14\uffff\1\11\17\uffff\1\10\2\uffff\1"+
			"\23\1\24\1\25\1\uffff\1\26\1\uffff\1\27\2\uffff\1\30\1\31\1\32\1\33\1"+
			"\34\1\35\3\uffff\1\36\1\uffff\1\37\1\40\1\41\1\42\1\uffff\1\43\1\44\1"+
			"\uffff\1\45\3\uffff\1\46\1\47\1\uffff\1\50\1\51\1\52\1\53\1\54\1\55\1"+
			"\56\6\uffff\1\57\1\60\1\61\136\uffff\1\62\1\uffff\1\21\1\22\1\7\1\13"+
			"\4\uffff\1\15\1\6\1\16\1\uffff\1\14\3\uffff\1\3\5\uffff\1\5\1\17",
			"\1\64",
			"",
			""
	};

	static final short[] DFA40_eot = DFA.unpackEncodedString(DFA40_eotS);
	static final short[] DFA40_eof = DFA.unpackEncodedString(DFA40_eofS);
	static final char[] DFA40_min = DFA.unpackEncodedStringToUnsignedChars(DFA40_minS);
	static final char[] DFA40_max = DFA.unpackEncodedStringToUnsignedChars(DFA40_maxS);
	static final short[] DFA40_accept = DFA.unpackEncodedString(DFA40_acceptS);
	static final short[] DFA40_special = DFA.unpackEncodedString(DFA40_specialS);
	static final short[][] DFA40_transition;

	static {
		int numStates = DFA40_transitionS.length;
		DFA40_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA40_transition[i] = DFA.unpackEncodedString(DFA40_transitionS[i]);
		}
	}

	protected class DFA40 extends DFA {

		public DFA40(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 40;
			this.eot = DFA40_eot;
			this.eof = DFA40_eof;
			this.min = DFA40_min;
			this.max = DFA40_max;
			this.accept = DFA40_accept;
			this.special = DFA40_special;
			this.transition = DFA40_transition;
		}
		@Override
		public String getDescription() {
			return "757:1: verification_error_reference : ( CLASS_DESCRIPTOR | field_reference | method_reference );";
		}
	}

	static final String DFA42_eotS =
		"\110\uffff";
	static final String DFA42_eofS =
		"\110\uffff";
	static final String DFA42_minS =
		"\1\5\105\uffff\1\0\1\uffff";
	static final String DFA42_maxS =
		"\1\u00d0\105\uffff\1\0\1\uffff";
	static final String DFA42_acceptS =
		"\1\uffff\1\2\105\uffff\1\1";
	static final String DFA42_specialS =
		"\106\uffff\1\0\1\uffff}>";
	static final String[] DFA42_transitionS = {
			"\1\106\1\uffff\1\1\5\uffff\2\1\5\uffff\1\1\7\uffff\2\1\1\uffff\1\1\3"+
			"\uffff\1\1\7\uffff\63\1\132\uffff\3\1\11\uffff\2\1\3\uffff\1\1\1\uffff"+
			"\2\1\2\uffff\2\1",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"",
			"\1\uffff",
			""
	};

	static final short[] DFA42_eot = DFA.unpackEncodedString(DFA42_eotS);
	static final short[] DFA42_eof = DFA.unpackEncodedString(DFA42_eofS);
	static final char[] DFA42_min = DFA.unpackEncodedStringToUnsignedChars(DFA42_minS);
	static final char[] DFA42_max = DFA.unpackEncodedStringToUnsignedChars(DFA42_maxS);
	static final short[] DFA42_accept = DFA.unpackEncodedString(DFA42_acceptS);
	static final short[] DFA42_special = DFA.unpackEncodedString(DFA42_specialS);
	static final short[][] DFA42_transition;

	static {
		int numStates = DFA42_transitionS.length;
		DFA42_transition = new short[numStates][];
		for (int i=0; i<numStates; i++) {
			DFA42_transition[i] = DFA.unpackEncodedString(DFA42_transitionS[i]);
		}
	}

	protected class DFA42 extends DFA {

		public DFA42(BaseRecognizer recognizer) {
			this.recognizer = recognizer;
			this.decisionNumber = 42;
			this.eot = DFA42_eot;
			this.eof = DFA42_eof;
			this.min = DFA42_min;
			this.max = DFA42_max;
			this.accept = DFA42_accept;
			this.special = DFA42_special;
			this.transition = DFA42_transition;
		}
		@Override
		public String getDescription() {
			return "()* loopback of 775:5: ({...}? annotation )*";
		}
		@Override
		public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
			TokenStream input = (TokenStream)_input;
			int _s = s;
			switch ( s ) {
					case 0 :
						int LA42_70 = input.LA(1);

						int index42_70 = input.index();
						input.rewind();
						s = -1;
						if ( ((input.LA(1) == ANNOTATION_DIRECTIVE)) ) {s = 71;}
						else if ( (true) ) {s = 1;}

						input.seek(index42_70);
						if ( s>=0 ) return s;
						break;
			}
			NoViableAltException nvae =
				new NoViableAltException(getDescription(), 42, _s, input);
			error(nvae);
			throw nvae;
		}
	}

	public static final BitSet FOLLOW_class_spec_in_smali_file1140 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_super_spec_in_smali_file1151 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_implements_spec_in_smali_file1159 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_source_spec_in_smali_file1168 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_method_in_smali_file1176 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_field_in_smali_file1182 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_annotation_in_smali_file1188 = new BitSet(new long[]{0x0000022000020020L,0x0000000000000000L,0x2000000000000000L,0x0000000000088000L});
	public static final BitSet FOLLOW_EOF_in_smali_file1199 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DIRECTIVE_in_class_spec1286 = new BitSet(new long[]{0x0000000000010010L});
	public static final BitSet FOLLOW_access_list_in_class_spec1288 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_class_spec1290 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUPER_DIRECTIVE_in_super_spec1308 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_super_spec1310 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_IMPLEMENTS_DIRECTIVE_in_implements_spec1329 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_implements_spec1331 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SOURCE_DIRECTIVE_in_source_spec1350 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_source_spec1352 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ACCESS_SPEC_in_access_list1371 = new BitSet(new long[]{0x0000000000000012L});
	public static final BitSet FOLLOW_FIELD_DIRECTIVE_in_field1402 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_access_list_in_field1404 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_member_name_in_field1406 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_COLON_in_field1408 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_field1410 = new BitSet(new long[]{0x0000001008000022L});
	public static final BitSet FOLLOW_EQUAL_in_field1413 = new BitSet(new long[]{0xA3F2B98401819950L,0x00000000381FD8B7L,0xD800000000000000L,0x00000000003665CFL});
	public static final BitSet FOLLOW_literal_in_field1415 = new BitSet(new long[]{0x0000000008000022L});
	public static final BitSet FOLLOW_annotation_in_field1428 = new BitSet(new long[]{0x0000000008000022L});
	public static final BitSet FOLLOW_END_FIELD_DIRECTIVE_in_field1442 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_METHOD_DIRECTIVE_in_method1553 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_access_list_in_method1555 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_member_name_in_method1557 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_method1559 = new BitSet(new long[]{0xFFFFF808301060A0L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_statements_and_directives_in_method1561 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_END_METHOD_DIRECTIVE_in_method1567 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ordered_method_item_in_statements_and_directives1612 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_registers_directive_in_statements_and_directives1620 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_catch_directive_in_statements_and_directives1628 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_catchall_directive_in_statements_and_directives1636 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_parameter_directive_in_statements_and_directives1644 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_annotation_in_statements_and_directives1652 = new BitSet(new long[]{0xFFFFF808101060A2L,0x000000003FFFFFFFL,0x0700000000000000L,0x0000000000019A30L});
	public static final BitSet FOLLOW_label_in_ordered_method_item1737 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_instruction_in_ordered_method_item1743 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_debug_directive_in_ordered_method_item1749 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_REGISTERS_DIRECTIVE_in_registers_directive1769 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_registers_directive1773 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LOCALS_DIRECTIVE_in_registers_directive1793 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_registers_directive1797 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PARAM_LIST_OR_ID_PRIMITIVE_TYPE_in_param_list_or_id1829 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_SIMPLE_NAME_in_simple_name1842 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ACCESS_SPEC_in_simple_name1848 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VERIFICATION_ERROR_TYPE_in_simple_name1859 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_POSITIVE_INTEGER_LITERAL_in_simple_name1870 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEGATIVE_INTEGER_LITERAL_in_simple_name1881 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_LITERAL_OR_ID_in_simple_name1892 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_LITERAL_OR_ID_in_simple_name1903 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_LITERAL_in_simple_name1914 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NULL_LITERAL_in_simple_name1925 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_REGISTER_in_simple_name1936 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_param_list_or_id_in_simple_name1947 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_simple_name1957 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_TYPE_in_simple_name1968 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ANNOTATION_VISIBILITY_in_simple_name1979 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_METHOD_HANDLE_TYPE_FIELD_in_simple_name1990 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_METHOD_HANDLE_TYPE_METHOD_in_simple_name1996 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10t_in_simple_name2002 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10x_in_simple_name2013 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10x_ODEX_in_simple_name2024 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT11x_in_simple_name2035 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT12x_OR_ID_in_simple_name2046 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_FIELD_in_simple_name2057 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_FIELD_ODEX_in_simple_name2068 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_simple_name2079 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_simple_name2090 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_simple_name2101 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_simple_name2112 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21t_in_simple_name2123 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_FIELD_in_simple_name2134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_FIELD_ODEX_in_simple_name2145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_simple_name2156 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22cs_FIELD_in_simple_name2167 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22s_OR_ID_in_simple_name2178 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22t_in_simple_name2189 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT23x_in_simple_name2200 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31i_OR_ID_in_simple_name2211 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31t_in_simple_name2222 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_simple_name2233 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_simple_name2244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_ODEX_in_simple_name2255 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_simple_name2266 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_simple_name2277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35mi_METHOD_in_simple_name2288 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35ms_METHOD_in_simple_name2299 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_simple_name2310 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_simple_name2321 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT51l_in_simple_name2332 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_name_in_member_name2347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_MEMBER_NAME_in_member_name2353 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OPEN_PAREN_in_method_prototype2368 = new BitSet(new long[]{0x0000000000090100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000140L});
	public static final BitSet FOLLOW_param_list_in_method_prototype2370 = new BitSet(new long[]{0x0000000000080000L});
	public static final BitSet FOLLOW_CLOSE_PAREN_in_method_prototype2372 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000200100L});
	public static final BitSet FOLLOW_type_descriptor_in_method_prototype2374 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PARAM_LIST_OR_ID_PRIMITIVE_TYPE_in_param_list_or_id_primitive_type2404 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_param_list_or_id_primitive_type_in_param_list2419 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000000L,0x0000000000000040L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_param_list2426 = new BitSet(new long[]{0x0000000000010102L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_ARRAY_TYPE_PREFIX_in_array_descriptor2437 = new BitSet(new long[]{0x0000000000010000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_set_in_array_descriptor2439 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_TYPE_in_type_descriptor2455 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_type_descriptor2461 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_type_descriptor2467 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_descriptor_in_type_descriptor2473 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_nonvoid_type_descriptor2483 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_nonvoid_type_descriptor2489 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_descriptor_in_nonvoid_type_descriptor2495 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_reference_type_descriptor2505 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_descriptor_in_reference_type_descriptor2511 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_POSITIVE_INTEGER_LITERAL_in_integer_literal2521 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NEGATIVE_INTEGER_LITERAL_in_integer_literal2532 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_LITERAL_OR_ID_in_float_literal2547 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_FLOAT_LITERAL_in_float_literal2558 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_LITERAL_OR_ID_in_double_literal2568 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_DOUBLE_LITERAL_in_double_literal2579 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONG_LITERAL_in_literal2589 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_literal2595 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORT_LITERAL_in_literal2601 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BYTE_LITERAL_in_literal2607 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_literal2613 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_double_literal_in_literal2619 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_literal2625 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_literal2631 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_LITERAL_in_literal2637 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_NULL_LITERAL_in_literal2643 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_array_literal_in_literal2649 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_subannotation_in_literal2655 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_type_field_method_literal_in_literal2661 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_enum_literal_in_literal2667 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_handle_literal_in_literal2673 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_prototype_in_literal2679 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_parsed_integer_literal2692 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONG_LITERAL_in_integral_literal2704 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_integral_literal2710 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORT_LITERAL_in_integral_literal2716 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_integral_literal2722 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BYTE_LITERAL_in_integral_literal2728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONG_LITERAL_in_fixed_32bit_literal2738 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_fixed_32bit_literal2744 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORT_LITERAL_in_fixed_32bit_literal2750 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BYTE_LITERAL_in_fixed_32bit_literal2756 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_fixed_32bit_literal2762 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_fixed_32bit_literal2768 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_LITERAL_in_fixed_32bit_literal2774 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_integer_literal_in_fixed_literal2784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LONG_LITERAL_in_fixed_literal2790 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SHORT_LITERAL_in_fixed_literal2796 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BYTE_LITERAL_in_fixed_literal2802 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_float_literal_in_fixed_literal2808 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_double_literal_in_fixed_literal2814 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CHAR_LITERAL_in_fixed_literal2820 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_BOOL_LITERAL_in_fixed_literal2826 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_array_literal2836 = new BitSet(new long[]{0xA3F2B98401859950L,0x00000000381FD8B7L,0xD800000000000000L,0x00000000003665CFL});
	public static final BitSet FOLLOW_literal_in_array_literal2839 = new BitSet(new long[]{0x0000000000240000L});
	public static final BitSet FOLLOW_COMMA_in_array_literal2842 = new BitSet(new long[]{0xA3F2B98401819950L,0x00000000381FD8B7L,0xD800000000000000L,0x00000000003665CFL});
	public static final BitSet FOLLOW_literal_in_array_literal2844 = new BitSet(new long[]{0x0000000000240000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_array_literal2852 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_name_in_annotation_element2876 = new BitSet(new long[]{0x0000001000000000L});
	public static final BitSet FOLLOW_EQUAL_in_annotation_element2878 = new BitSet(new long[]{0xA3F2B98401819950L,0x00000000381FD8B7L,0xD800000000000000L,0x00000000003665CFL});
	public static final BitSet FOLLOW_literal_in_annotation_element2880 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ANNOTATION_DIRECTIVE_in_annotation2905 = new BitSet(new long[]{0x0000000000000040L});
	public static final BitSet FOLLOW_ANNOTATION_VISIBILITY_in_annotation2907 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_annotation2909 = new BitSet(new long[]{0xA3F2B90003000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_annotation_element_in_annotation2915 = new BitSet(new long[]{0xA3F2B90003000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_END_ANNOTATION_DIRECTIVE_in_annotation2918 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SUBANNOTATION_DIRECTIVE_in_subannotation2951 = new BitSet(new long[]{0x0000000000010000L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_subannotation2953 = new BitSet(new long[]{0xA3F2B90201000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_annotation_element_in_subannotation2955 = new BitSet(new long[]{0xA3F2B90201000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_END_SUBANNOTATION_DIRECTIVE_in_subannotation2958 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ENUM_DIRECTIVE_in_enum_literal2985 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_enum_literal2987 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_type_field_method_literal3007 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_type_field_method_literal3016 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ARROW_in_type_field_method_literal3018 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_member_name_in_type_field_method_literal3030 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_COLON_in_type_field_method_literal3032 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_type_field_method_literal3034 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_member_name_in_type_field_method_literal3057 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_type_field_method_literal3059 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PRIMITIVE_TYPE_in_type_field_method_literal3092 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_VOID_TYPE_in_type_field_method_literal3098 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simple_name_in_call_site_reference3108 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_OPEN_PAREN_in_call_site_reference3110 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_call_site_reference3112 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_call_site_reference3114 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_call_site_reference3116 = new BitSet(new long[]{0x0000000000280000L});
	public static final BitSet FOLLOW_COMMA_in_call_site_reference3119 = new BitSet(new long[]{0xA3F2B98401819950L,0x00000000381FD8B7L,0xD800000000000000L,0x00000000003665CFL});
	public static final BitSet FOLLOW_literal_in_call_site_reference3121 = new BitSet(new long[]{0x0000000000280000L});
	public static final BitSet FOLLOW_CLOSE_PAREN_in_call_site_reference3125 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_AT_in_call_site_reference3127 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_call_site_reference3129 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_METHOD_HANDLE_TYPE_FIELD_in_method_handle_reference3173 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_AT_in_method_handle_reference3175 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_method_handle_reference3177 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_METHOD_HANDLE_TYPE_METHOD_in_method_handle_reference3189 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_AT_in_method_handle_reference3191 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_method_handle_reference3193 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_method_handle_reference3205 = new BitSet(new long[]{0x0000000000000400L});
	public static final BitSet FOLLOW_AT_in_method_handle_reference3207 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_method_handle_reference3209 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_handle_reference_in_method_handle_literal3225 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_method_reference3246 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ARROW_in_method_reference3248 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_member_name_in_method_reference3252 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_method_reference3254 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_reference_type_descriptor_in_field_reference3276 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ARROW_in_field_reference3278 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_member_name_in_field_reference3282 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_COLON_in_field_reference3284 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_field_reference3286 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_label3307 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_simple_name_in_label3309 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_COLON_in_label_ref3328 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_simple_name_in_label_ref3330 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_REGISTER_in_register_list3344 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_COMMA_in_register_list3347 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_register_list3349 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_REGISTER_in_register_range3384 = new BitSet(new long[]{0x0000000000400002L});
	public static final BitSet FOLLOW_DOTDOT_in_register_range3387 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_register_range3391 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CLASS_DESCRIPTOR_in_verification_error_reference3420 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_field_reference_in_verification_error_reference3424 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_method_reference_in_verification_error_reference3428 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CATCH_DIRECTIVE_in_catch_directive3438 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_catch_directive3440 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_catch_directive3442 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive3446 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_DOTDOT_in_catch_directive3448 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive3452 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_catch_directive3454 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catch_directive3458 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_CATCHALL_DIRECTIVE_in_catchall_directive3490 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_catchall_directive3492 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive3496 = new BitSet(new long[]{0x0000000000400000L});
	public static final BitSet FOLLOW_DOTDOT_in_catchall_directive3498 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive3502 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_catchall_directive3504 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_catchall_directive3508 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PARAMETER_DIRECTIVE_in_parameter_directive3547 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_parameter_directive3549 = new BitSet(new long[]{0x0000000080200022L});
	public static final BitSet FOLLOW_COMMA_in_parameter_directive3552 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_parameter_directive3554 = new BitSet(new long[]{0x0000000080000022L});
	public static final BitSet FOLLOW_annotation_in_parameter_directive3565 = new BitSet(new long[]{0x0000000080000022L});
	public static final BitSet FOLLOW_END_PARAMETER_DIRECTIVE_in_parameter_directive3578 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_line_directive_in_debug_directive3651 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_local_directive_in_debug_directive3657 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_end_local_directive_in_debug_directive3663 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_restart_local_directive_in_debug_directive3669 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prologue_directive_in_debug_directive3675 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_epilogue_directive_in_debug_directive3681 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_source_directive_in_debug_directive3687 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LINE_DIRECTIVE_in_line_directive3697 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_line_directive3699 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_LOCAL_DIRECTIVE_in_local_directive3722 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_local_directive3724 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_COMMA_in_local_directive3727 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020002L});
	public static final BitSet FOLLOW_NULL_LITERAL_in_local_directive3730 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_local_directive3736 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_COLON_in_local_directive3739 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000200100L});
	public static final BitSet FOLLOW_VOID_TYPE_in_local_directive3742 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_local_directive3746 = new BitSet(new long[]{0x0000000000200002L});
	public static final BitSet FOLLOW_COMMA_in_local_directive3780 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_local_directive3784 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_END_LOCAL_DIRECTIVE_in_end_local_directive3826 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_end_local_directive3828 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_RESTART_LOCAL_DIRECTIVE_in_restart_local_directive3851 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_restart_local_directive3853 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PROLOGUE_DIRECTIVE_in_prologue_directive3876 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_EPILOGUE_DIRECTIVE_in_epilogue_directive3897 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SOURCE_DIRECTIVE_in_source_directive3918 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_source_directive3920 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT12x_in_instruction_format12x3945 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT12x_OR_ID_in_instruction_format12x3951 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22s_in_instruction_format22s3966 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22s_OR_ID_in_instruction_format22s3972 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31i_in_instruction_format31i3987 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31i_OR_ID_in_instruction_format31i3993 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_in_instruction_format35c_method4010 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_OR_METHOD_HANDLE_TYPE_in_instruction_format35c_method4016 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format10t_in_instruction4031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format10x_in_instruction4037 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format10x_odex_in_instruction4043 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format11n_in_instruction4049 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format11x_in_instruction4055 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format12x_in_instruction4061 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format20bc_in_instruction4067 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format20t_in_instruction4073 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_field_in_instruction4079 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_field_odex_in_instruction4085 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_method_handle_in_instruction4091 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_method_type_in_instruction4097 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_string_in_instruction4103 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21c_type_in_instruction4109 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21ih_in_instruction4115 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21lh_in_instruction4121 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21s_in_instruction4127 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format21t_in_instruction4133 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22b_in_instruction4139 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22c_field_in_instruction4145 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22c_field_odex_in_instruction4151 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22c_type_in_instruction4157 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22cs_field_in_instruction4163 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22s_in_instruction4169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22t_in_instruction4175 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format22x_in_instruction4181 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format23x_in_instruction4187 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format30t_in_instruction4193 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31c_in_instruction4199 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31i_in_instruction4205 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format31t_in_instruction4211 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format32x_in_instruction4217 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_call_site_in_instruction4223 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_method_in_instruction4229 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_type_in_instruction4235 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35c_method_odex_in_instruction4241 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35mi_method_in_instruction4247 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format35ms_method_in_instruction4253 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_call_site_in_instruction4259 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_method_in_instruction4265 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_method_odex_in_instruction4271 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rc_type_in_instruction4277 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rmi_method_in_instruction4283 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format3rms_method_in_instruction4289 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format45cc_method_in_instruction4295 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format4rcc_method_in_instruction4301 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_format51l_in_instruction4307 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_array_data_directive_in_instruction4313 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_packed_switch_directive_in_instruction4319 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_insn_sparse_switch_directive_in_instruction4325 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10t_in_insn_format10t4345 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format10t4347 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10x_in_insn_format10x4377 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT10x_ODEX_in_insn_format10x_odex4405 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT11n_in_insn_format11n4426 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format11n4428 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format11n4430 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_insn_format11n4432 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT11x_in_insn_format11x4464 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format11x4466 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_instruction_format12x_in_insn_format12x4496 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format12x4498 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format12x4500 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format12x4502 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT20bc_in_insn_format20bc4534 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000100000L});
	public static final BitSet FOLLOW_VERIFICATION_ERROR_TYPE_in_insn_format20bc4536 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format20bc4538 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_verification_error_reference_in_insn_format20bc4540 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT20t_in_insn_format20t4577 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format20t4579 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_FIELD_in_insn_format21c_field4609 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_field4611 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_field4613 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_insn_format21c_field4615 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_FIELD_ODEX_in_insn_format21c_field_odex4647 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_field_odex4649 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_field_odex4651 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_insn_format21c_field_odex4653 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_HANDLE_in_insn_format21c_method_handle4691 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_method_handle4693 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_method_handle4695 = new BitSet(new long[]{0x0000000000000000L,0x0000000000020000L,0xC000000000000000L});
	public static final BitSet FOLLOW_method_handle_reference_in_insn_format21c_method_handle4697 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_METHOD_TYPE_in_insn_format21c_method_type4743 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_method_type4745 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_method_type4747 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format21c_method_type4749 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_STRING_in_insn_format21c_string4793 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_string4795 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_string4797 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_insn_format21c_string4799 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21c_TYPE_in_insn_format21c_type4831 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21c_type4833 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21c_type4835 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format21c_type4837 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21ih_in_insn_format21ih4869 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21ih4871 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21ih4873 = new BitSet(new long[]{0x0000018000009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_format21ih4875 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21lh_in_insn_format21lh4907 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21lh4909 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21lh4911 = new BitSet(new long[]{0x0000018000009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_format21lh4913 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21s_in_insn_format21s4945 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21s4947 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21s4949 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_insn_format21s4951 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT21t_in_insn_format21t4983 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format21t4985 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format21t4987 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format21t4989 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22b_in_insn_format22b5021 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22b5023 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22b5025 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22b5027 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22b5029 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_insn_format22b5031 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_FIELD_in_insn_format22c_field5065 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field5067 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_field5069 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field5071 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_field5073 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_insn_format22c_field5075 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_FIELD_ODEX_in_insn_format22c_field_odex5109 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field_odex5111 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_field_odex5113 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_field_odex5115 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_field_odex5117 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_field_reference_in_insn_format22c_field_odex5119 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22c_TYPE_in_insn_format22c_type5159 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_type5161 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_type5163 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22c_type5165 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22c_type5167 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format22c_type5169 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22cs_FIELD_in_insn_format22cs_field5203 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22cs_field5205 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22cs_field5207 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22cs_field5209 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22cs_field5211 = new BitSet(new long[]{0x0000004000000000L});
	public static final BitSet FOLLOW_FIELD_OFFSET_in_insn_format22cs_field5213 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_instruction_format22s_in_insn_format22s5234 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22s5236 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22s5238 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22s5240 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22s5242 = new BitSet(new long[]{0x0000000000009000L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_integral_literal_in_insn_format22s5244 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22t_in_insn_format22t5278 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22t5280 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22t5282 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22t5284 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22t5286 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format22t5288 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT22x_in_insn_format22x5322 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22x5324 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format22x5326 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format22x5328 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT23x_in_insn_format23x5360 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x5362 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format23x5364 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x5366 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format23x5368 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format23x5370 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT30t_in_insn_format30t5404 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format30t5406 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31c_in_insn_format31c5436 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31c5438 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format31c5440 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000020000L});
	public static final BitSet FOLLOW_STRING_LITERAL_in_insn_format31c5442 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_instruction_format31i_in_insn_format31i5473 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31i5475 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format31i5477 = new BitSet(new long[]{0x0000018000009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_format31i5479 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT31t_in_insn_format31t5511 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format31t5513 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format31t5515 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_format31t5517 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT32x_in_insn_format32x5549 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format32x5551 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format32x5553 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format32x5555 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_CALL_SITE_in_insn_format35c_call_site5592 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35c_call_site5594 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_call_site5596 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35c_call_site5598 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35c_call_site5600 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_call_site_reference_in_insn_format35c_call_site5602 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_instruction_format35c_method_in_insn_format35c_method5634 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35c_method5636 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_method5638 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35c_method5640 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35c_method5642 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format35c_method5644 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_TYPE_in_insn_format35c_type5676 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35c_type5678 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_type5680 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35c_type5682 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35c_type5684 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format35c_type5686 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35c_METHOD_ODEX_in_insn_format35c_method_odex5718 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35c_method_odex5720 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35c_method_odex5722 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35c_method_odex5724 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35c_method_odex5726 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format35c_method_odex5728 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35mi_METHOD_in_insn_format35mi_method5749 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35mi_method5751 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35mi_method5753 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35mi_method5755 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35mi_method5757 = new BitSet(new long[]{0x0000040000000000L});
	public static final BitSet FOLLOW_INLINE_INDEX_in_insn_format35mi_method5759 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT35ms_METHOD_in_insn_format35ms_method5780 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format35ms_method5782 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format35ms_method5784 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format35ms_method5786 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format35ms_method5788 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000400000L});
	public static final BitSet FOLLOW_VTABLE_INDEX_in_insn_format35ms_method5790 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_CALL_SITE_in_insn_format3rc_call_site5816 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rc_call_site5818 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_call_site5820 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rc_call_site5822 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rc_call_site5824 = new BitSet(new long[]{0xA3F2B90001000850L,0x00000000381FD8B7L,0xC000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_call_site_reference_in_insn_format3rc_call_site5826 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_in_insn_format3rc_method5858 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rc_method5860 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_method5862 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rc_method5864 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rc_method5866 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format3rc_method5868 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_METHOD_ODEX_in_insn_format3rc_method_odex5900 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rc_method_odex5902 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format3rc_method_odex5904 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rc_method_odex5906 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rc_method_odex5908 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format3rc_method_odex5910 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rc_TYPE_in_insn_format3rc_type5931 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rc_type5933 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rc_type5935 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rc_type5937 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rc_type5939 = new BitSet(new long[]{0x0000000000010100L,0x0000000000000000L,0x0000000000000000L,0x0000000000000100L});
	public static final BitSet FOLLOW_nonvoid_type_descriptor_in_insn_format3rc_type5941 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rmi_METHOD_in_insn_format3rmi_method5973 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rmi_method5975 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rmi_method5977 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rmi_method5979 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rmi_method5981 = new BitSet(new long[]{0x0000040000000000L});
	public static final BitSet FOLLOW_INLINE_INDEX_in_insn_format3rmi_method5983 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT3rms_METHOD_in_insn_format3rms_method6004 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format3rms_method6006 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format3rms_method6008 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format3rms_method6010 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format3rms_method6012 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000400000L});
	public static final BitSet FOLLOW_VTABLE_INDEX_in_insn_format3rms_method6014 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT45cc_METHOD_in_insn_format45cc_method6035 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format45cc_method6037 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_list_in_insn_format45cc_method6039 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format45cc_method6041 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format45cc_method6043 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format45cc_method6045 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format45cc_method6047 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format45cc_method6049 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT4rcc_METHOD_in_insn_format4rcc_method6083 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000004L});
	public static final BitSet FOLLOW_OPEN_BRACE_in_insn_format4rcc_method6085 = new BitSet(new long[]{0x0000000000040000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_register_range_in_insn_format4rcc_method6087 = new BitSet(new long[]{0x0000000000040000L});
	public static final BitSet FOLLOW_CLOSE_BRACE_in_insn_format4rcc_method6089 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format4rcc_method6091 = new BitSet(new long[]{0xA3F2B90001010950L,0x00000000381FD8B7L,0xD000000000000000L,0x00000000003045C3L});
	public static final BitSet FOLLOW_method_reference_in_insn_format4rcc_method6093 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format4rcc_method6095 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000008L});
	public static final BitSet FOLLOW_method_prototype_in_insn_format4rcc_method6097 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_INSTRUCTION_FORMAT51l_in_insn_format51l6131 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000400L});
	public static final BitSet FOLLOW_REGISTER_in_insn_format51l6133 = new BitSet(new long[]{0x0000000000200000L});
	public static final BitSet FOLLOW_COMMA_in_insn_format51l6135 = new BitSet(new long[]{0x0000018001809800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_literal_in_insn_format51l6137 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_ARRAY_DATA_DIRECTIVE_in_insn_array_data_directive6164 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000000L,0x0000000000000000L,0x0000000000000081L});
	public static final BitSet FOLLOW_parsed_integer_literal_in_insn_array_data_directive6170 = new BitSet(new long[]{0x0000018005809800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_literal_in_insn_array_data_directive6182 = new BitSet(new long[]{0x0000018005809800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_END_ARRAY_DATA_DIRECTIVE_in_insn_array_data_directive6185 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_PACKED_SWITCH_DIRECTIVE_in_insn_packed_switch_directive6231 = new BitSet(new long[]{0x0000018000009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_packed_switch_directive6237 = new BitSet(new long[]{0x0000000040100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_packed_switch_directive6243 = new BitSet(new long[]{0x0000000040100000L});
	public static final BitSet FOLLOW_END_PACKED_SWITCH_DIRECTIVE_in_insn_packed_switch_directive6250 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_SPARSE_SWITCH_DIRECTIVE_in_insn_sparse_switch_directive6324 = new BitSet(new long[]{0x0000018100009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_fixed_32bit_literal_in_insn_sparse_switch_directive6331 = new BitSet(new long[]{0x0000000000000200L});
	public static final BitSet FOLLOW_ARROW_in_insn_sparse_switch_directive6333 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_label_ref_in_insn_sparse_switch_directive6335 = new BitSet(new long[]{0x0000018100009800L,0x0000000000000000L,0x0800000000000000L,0x0000000000002081L});
	public static final BitSet FOLLOW_END_SPARSE_SWITCH_DIRECTIVE_in_insn_sparse_switch_directive6343 = new BitSet(new long[]{0x0000000000000002L});
}
