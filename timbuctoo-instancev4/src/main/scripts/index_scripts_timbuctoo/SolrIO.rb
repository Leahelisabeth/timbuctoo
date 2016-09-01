require "net/http"

class SolrIO

    @bad_batches = 1
    @wanted_document_receptions = Array.new
    @solr = ""
    @solr_auth = ""

    def initialize solr_adres,solr_auth=""
	@solr = solr_adres
	@solr_auth = aolr_auth
    end

    def create index_name
    end

    def update index_name,batch
	uri = URI.parse("#{@solr}update/")
	req = Net::HTTP::Post.new(uri)
	req.content_type = "application/json"
	req["Authorization"] = @solr_auth
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.body = batch.to_json
	response = http.request(req)
	if response.code.eql?("400")
	  File.open("bad_batch_#{@bad_batches}.json", 'w') { |file| file.write(batch.to_json) }
	  @bad_batches += 1
	  puts "BAD BATCH"
	end
    end

    def commit index_name
	puts "COMMIT document receptions"
	uri = URI.parse("#{@solr}update?commit=true")
	req = Net::HTTP::Post.new(uri)
	req["Authorization"] = @solr_auth
	http = Net::HTTP.new(uri.hostname, uri.port)
	http.request(req)
    end

    def delete index_name
	puts "DELETE document receptions index"
	uri = URI.parse("#{@solr}update/")
	req = Net::HTTP::Post.new(uri)
	http = Net::HTTP.new(uri.hostname, uri.port)
	req.content_type = 'text/xml'
	req.body = '<delete><query>*:*</query></delete>'
	response = http.request(req)
	if !response.code.eql?("200")
	  puts "SOMETHING WENT WRONG: DocumentReceptions.delete_index"
	end
    end

end

