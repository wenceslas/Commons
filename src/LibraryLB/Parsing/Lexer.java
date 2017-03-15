/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package LibraryLB.Parsing;

import LibraryLB.Containers.SelfSortingMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

/**
 *
 * @author Lemmin
 */
public class Lexer {
    public enum TokenType{
        LITERAL("LITERAL"),
        STRING("STRING");
        public String type;
        TokenType(String n){
            this.type = n;
        }
    }
    public class LexerException extends Exception{}
    public class NoSuchLexemeException extends LexerException{}
    public class StringNotTerminatedException extends LexerException{}
    private final Comparator cmp = (Comparator) (Object t, Object t1) -> {
        String s1 = (String)t;
        String s2 = (String)t1;
        int len = s2.length() - s1.length();
        if(len ==0){
            len = 1;
        }
        return len;
    };
    
    
    protected String   keyStringBegin,
                    keyStringEnd,
                    keyStringEscape;
    protected SelfSortingMap<String,String> keywords;
    protected int linePos,charPos = 0;
    protected ArrayList<String> lines;
    public boolean skipWhitespace;
    public Lexer(Collection<String> lines){
        this.keywords = new SelfSortingMap<>(cmp);
        this.lines = new ArrayList<>();
        this.lines.addAll(lines);
    }
    public Lexer(String line){
        this.keywords = new SelfSortingMap<>(cmp);
        this.lines = new ArrayList<>();
        this.lines.add(line);
    }
    public void addToken(String...tokens){
        for(String tok:tokens){
            this.keywords.put(tok, tok);
        }
    }
    public void addToken(Collection<String> tokens){
        tokens.forEach(tok ->{
            this.addToken(tok);
        });
    }
    protected Character getCurrentChar(){
        Integer[] pos = new Integer[]{this.linePos,this.charPos};
        return this.getByPos(pos);
    }    
    protected Integer currentLineLen(){
        if(this.lines.size()>linePos){
            return this.lines.get(linePos).length();
        }else{
            return null;
        }
    }   
    protected Character getByPos(Integer[] pos){
        Character ch = null;       
        try{
            ch = this.lines.get(pos[0]).charAt(pos[1]);
        }catch(Exception e){}
        return ch;
    }
    protected Integer[] rangeCheck(Integer shift){
        Integer[] pos = new Integer[2];
        pos[0] = this.linePos;
        pos[1] = this.charPos+shift;
        if(this.currentLineLen() == null){
            return pos;
        }
        while(pos[1] >= this.currentLineLen()){
            pos[1]-= this.currentLineLen();
            pos[0]+=1;
            if(pos[0]>=this.lines.size()){
                pos[0] = this.lines.size();
                break;
            }
        }
        return pos;
    } 
    protected Character peek(Integer peek){
        Integer[] rangeCheck = this.rangeCheck(peek);
        if(rangeCheck[0]!=this.lines.size()){
            return this.getByPos(rangeCheck);
        }
        return null;
    }
    protected Character advance(Integer am){
        Integer[] pos;
        pos = this.rangeCheck(am);
        this.linePos = pos[0];
        this.charPos = pos[1];
        return this.getByPos(pos);   
    }
    public boolean hasStrings(){
        return !(this.keyStringBegin==null||this.keyStringEnd==null||this.keyStringEscape==null);
    }
    public void prepareForStrings(String strBeg, String strEnd, String strEsc){     
        this.keywords.put(strBeg, strBeg);
        this.keywords.put(strEnd, strEnd);
        this.keywords.put(strEsc, strEsc);
        this.keyStringBegin = strBeg;
        this.keyStringEnd = strEnd;
        this.keyStringEscape = strEsc;
        
    }
    public void reset(){
        this.charPos = 0;
        this.linePos = 0;
    }
    protected void skipWhitespace(){        
        while(true){
            Character c = this.getCurrentChar();
            if(c==null){
                return;
            }else{
                if(!Character.isWhitespace(c)){
                    return;
                }
                this.advance(1);
            }
        }
        
    }
    protected Token string() throws StringNotTerminatedException, NoSuchLexemeException{
        String result = "";
        while(true){
            Character ch = this.getCurrentChar();
            if(ch == null){
                throw new StringNotTerminatedException();             
            }else if(this.tryToMatch(this.keyStringEscape)){
                this.advanceByTokenKey(this.keyStringEscape);
                result += this.getCurrentChar();
                this.advance(1);
            }
            if(this.tryToMatch(this.keyStringEnd)){
                this.advanceByTokenKey(this.keyStringEnd);
                break;
            }
            
            result += this.getCurrentChar();
            this.advance(1);
             
        }
        return new Literal(TokenType.STRING.type,this.rangeCheck(0),result);
        
    }
    protected Token keyword(){       
        for(String token:this.keywords.getOrderedList()){
            if(this.tryToMatch(token)){
                return new Token(token,new Integer[]{this.linePos,this.charPos});
            }
        }
        return null;
    }
    protected boolean tryToMatch(String explicit){
        int lenToPeek = explicit.length();
        String readSymbols = "";
        for(int i = 0; i < lenToPeek; i++){
            Character ch = this.peek(i);
            if (ch != null){
                readSymbols+=ch;
            }else{
                break;
            }
        }
        return readSymbols.equals(explicit);
    }
    protected String getTokenID(String key){
        return this.keywords.get(key);
    }
    protected void advanceByTokenKey(String key){
        int len = this.keywords.get(key).length();
        this.advance(len);
    }
    protected Token literal(String value,Integer[] pos){
        return new Literal(TokenType.LITERAL.type,pos,value);
    }
    protected Token getNextTokenImpl(){
        return null;
    }
    public Token getNextToken() throws NoSuchLexemeException, StringNotTerminatedException{
        String buffer = "";
        Token token = getNextTokenImpl();
        if(token!=null){
            return token;
        }
        Integer[] pos = new Integer[]{this.linePos,this.charPos};
        
        while(true){
            
            Character ch = this.getCurrentChar();
            if(ch==null){
                if(buffer.length()>0){
                    return this.literal(buffer,pos);
                }
                break;
            }
            if(this.skipWhitespace){
                if(Character.isWhitespace(ch)){ 
                    this.advance(1);
                    if(buffer.length()>0){
                        return this.literal(buffer,pos);
                    }
                    continue;
                }
            }
            if(this.hasStrings()){
                if(this.tryToMatch(this.keyStringBegin)){
                    this.advanceByTokenKey(this.keyStringBegin);
                    return this.string();
                }
            }          
            token = this.keyword();
            if(token!=null){
                if(buffer.length()>0){
                    return this.literal(buffer,pos);
                }else{
                    this.advanceByTokenKey(token.id);
                    return token;
                }
                
            }else{
                buffer+=ch; 
                this.advance(1);
            }
            
        }
        return token;
    }
    public Collection<Token> getRemainingTokens() throws NoSuchLexemeException, StringNotTerminatedException{
        ArrayList<Token> remains = new ArrayList<>();
        while(true){
            Token nextToken = this.getNextToken();
            if(nextToken == null){
                break;
            }
            remains.add(nextToken);
        }
        return remains;
    }
}
