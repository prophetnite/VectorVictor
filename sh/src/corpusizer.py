'''
Created on Jan 11, 2014

@author: Erik
'''

import gensim, bz2, os

def extract(dirname):
    for fname in os.listdir(dirname):
        print fname
        texts = gensim.corpora.wikicorpus._extract_pages(bz2.BZ2File(os.path.join(dirname, fname))) # generator

        for text in texts:
            for sentence in text:
                s = process_article(text)
                if(len(s)>50):
                    yield s
                else:
                    continue

def process_article(document):
    text = gensim.corpora.wikicorpus.filter_wiki(str(document)) # remove markup, get plain text
    return gensim.corpora.wikicorpus.tokenize(text) # tokenize plain text

def is_number(s):
    try:
        float(s)
        return True
    except ValueError:
        return False

def yield_vocab():
    lines = open('enwiki_wordids.txt').readlines()
    sentence = []
    for line in lines:
        for word in line.split():
            if not is_number(word):
                sentence.append(word)
    return sentence


def build(fname, dirname = '../enwiki/'):
    print 'starting corpus build...'
    model = gensim.models.Word2Vec(size=400, window=5, min_count=10, workers=4)
    model.build_vocab(extract(dirname))
    model.train(extract(dirname))
    print 'model built'
    try:
        model.save(fname)
        print('model saved')
    except:
        print('model not saved')


def augment(fname, dirname):
    print 'augmenting ' + fname + ' with files in ' + dirname
    model = gensim.models.Word2Vec.load(fname)
    model.train(extract(dirname))
    print 'model built'
    try:
        model.save(fname)
        print('model saved')
    except:
        print('model not saved')

#augment('../vecmodel-partial-400.bin','../enwiki-to-process')

