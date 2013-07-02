/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/

/***

client.pets.exams.tech.phones.number = '7707951623'


-- client.pets.exams.examItems.item.sectionItems.examItemStatus.name like 'Re%' OR client.pets.exams.examItems == null
--    AND client.pets.exams.examItems.item.name != 'Test'

select * from client where
exists (select * from pet where pet.clientId = client.id and
exists (select * from exam where exam.petId = pet.id and
exists (select * from examItem, item where examItem.examId = exam.id and examItem.itemId = item.id AND item.name <> 'Test' AND
exists (select * from sectionItem, examItemStatus where sectionItem.itemId = item.id and examItemStatus.name like 'Re%')))
OR (0 = (select count(*) from exam where exam.pet = pet.id))
)


-- client.pets.exams.examItems.item.sectionItems.examItemStatus.name like 'Re%'

select * from client where
exists (select * from pet where pet.clientId = client.id and
exists (select * from exam where exam.petId = pet.id and
exists (select * from examItem, item where examItem.examId = exam.id and examItem.itemId = item.id and
exists (select * from sectionItem, examItemStatus where sectionItem.itemId = item.id and examItemStatus.name like 'Re%'))))

-- client.pets.exams.examItems.item.sectionItems.examItemStatus = null

select * from client where
exists (select * from pet where pet.clientId = client.id and
exists (select * from exam where exam.petId = pet.id and
exists (select * from examItem, item where examItem.examId = exam.id and examItem.itemId = item.id and
exists (select * from sectionItem where sectionItem.examItemStatusId is null))))


-- client.pets.exams.examItems.item.sectionItems.examItemStatus != null

select * from client where
exists (select * from pet where pet.clientId = client.id and
exists (select * from exam where exam.petId = pet.id and
exists (select * from examItem, item where examItem.examId = exam.id and examItem.itemId = item.id and
exists (select * from sectionItem where sectionItem.examItemStatusId is not null))))

-- client.pets.exams != null

select * from client where
exists (select * from pet where pet.clientId = client.id and
0 < (select count(*) from exam where exam.petId = pet.id))


-- client.pets.exams == null

select * from client where
exists (select * from pet where pet.clientId = client.id and
0 = (select count(*) from exam where exam.petId = pet.id))

***/


package com.viaoa.ds.query;

import java.util.*;

/**
    Descendent parser internally used to parse object queries into a Vector of OAQueryToken Objects.
    Uses a OAQueryTokenManager to parse into tokens.
    <p>
    For more information about this package, see <a href="package-summary.html#package_description">documentation</a>.
*/
public class OAQueryTokenizer implements OAQueryTokenType {
    OAQueryTokenManager tokenManager;
    OAQueryToken token, lastToken;
    Vector vec;



    /** convert query to vector of tokens */
    public Vector convertToTokens(String query) {
        if (tokenManager == null) tokenManager = new OAQueryTokenManager();
        vec = new Vector(20,20);
        tokenManager.setQuery(query);
        nextToken();
        evaluate();
        return vec;
    }

    protected void evaluate() {
        evaluateA();
        if (token.type != OAQueryTokenType.EOF) {
            throw new RuntimeException("unexpected token \""+token.value+"\" while parsing query " + tokenManager.query);
        }
    }

    // AND OR
    protected void evaluateA() {
        evaluateB();
        if (token.type == OAQueryTokenType.AND || token.type == OAQueryTokenType.OR) {
            vec.addElement(token);
            nextToken();
            evaluateA();
        }
    }

    // GT, GE, LT, LE, EQUAL, NOTEQUAL, LIKE
    protected void evaluateB() {
        evaluateC();
        if (token.isOperator()) {
            vec.addElement(token);
            nextToken();
            evaluateA();
        }
    }

    // () used to surround
    protected void evaluateC() {
        if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
            vec.addElement(token);
            nextToken();
            evaluateA();

            if (token.type == OAQueryTokenType.SEPERATOREND) {
                vec.addElement(token);
                nextToken();
            }
            else throw new RuntimeException("Unbalanced brackets in query " + tokenManager.query);
        }
        else evaluateC2();
    }
    
// 20090608 added C2, to allow for sql functions, ex: lower(lastName)
    // () func call
    protected void evaluateC2() {
        evaluateD();
        if (token.type == OAQueryTokenType.SEPERATORBEGIN) {
            token.type = OAQueryTokenType.FUNCTIONBEGIN;
            vec.addElement(token);
            nextToken();
            
            evaluateA();
            if (token.type == OAQueryTokenType.SEPERATOREND) {
                token.type = OAQueryTokenType.FUNCTIONEND;
                vec.addElement(token);
                nextToken();
            }
            else throw new RuntimeException("Unbalanced brackets in query " + tokenManager.query);
        }
    }

    
    // single quotes
    protected void evaluateD() {
        evaluateE();
        if (token.type == OAQueryTokenType.STRINGSQ) {
            vec.addElement(token);
            nextToken();
        }
    }


    // Single Quote
    protected void evaluateE() {
        // sql allows for single quotes to be doubled up to show a single quote in string
        evaluateF();
        while (token.type == OAQueryTokenType.STRINGSQ) {
            vec.addElement(token);
            nextToken();
        }
    }


    // VARIABLE, NUMBER, EOF, ?
    protected void evaluateF() {
        if ((token.type == OAQueryTokenType.STRINGDQ) ||
            (token.type == OAQueryTokenType.STRINGSQ) ||
            (token.type == OAQueryTokenType.STRINGESC) ||
            (token.type == OAQueryTokenType.NUMBER) ||
            (token.type == OAQueryTokenType.NULL) ||
            (token.type == OAQueryTokenType.PASSTHRU) ||
            (token.type == OAQueryTokenType.VARIABLE) || 
            (token.type == OAQueryTokenType.QUESTION)) {
            vec.addElement(token);
            nextToken();
        }
        else {
            throw new RuntimeException("Unexpected value in query " + tokenManager.query + " expecting variable or string, received "+token.value);
        }
    }

    protected void nextToken() {
        lastToken = token;
        token = tokenManager.getNext();
    }

    public static void main(String[] args) {
        OAQueryTokenizer qt = new OAQueryTokenizer();
        String query = "Code = 'CT13''6\"X16HALF-COL'";
        Vector vec = qt.convertToTokens(query);
        int x = vec.size();
    }
    
}



