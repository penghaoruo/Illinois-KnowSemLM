import gensim

fname = "/shared/preprocessed/resources/SRLGROUP_DOC.txt"
sentences = gensim.models.word2vec.LineSentence(fname)

#model = gensim.models.Word2Vec(sentences, size=300, sg=0, window=5, min_count=5, workers=16)
model = gensim.models.Word2Vec(sentences, size=300, sg=1, window=10, min_count=5, workers=16)
model.save("frameLM/w2v-srlgroup-sg.lm")
