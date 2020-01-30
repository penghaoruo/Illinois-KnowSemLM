import gensim

model_name = "frameLM/w2v-srl.lm"
model = gensim.models.Word2Vec.load(model_name)

fname = "/shared/preprocessed/resources/SRL_DOC_test_tmp.txt"
sentences = gensim.models.word2vec.LineSentence(fname)

print model.score(sentences, total_sentences=7, chunksize=100, queue_factor=2, report_delay=1)
print model['yield.01']
