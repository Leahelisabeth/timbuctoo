require 'open-uri'
require 'pp'
require './timer.rb'
require './TimbuctooIO.rb'
require './SolrIO.rb'
require './DefaultMapper.rb'
require 'json'


if __FILE__ == $0

    @location = "http://test.repository.huygens.knaw.nl/"
    @solr_auth_header = ""
    @solr = "http://192.168.99.100:8983/solr/"

    begin
	(0..(ARGV.size-1)).each do |i|
	  case ARGV[i]
	    when '--debug'
		debug = true
	    when '-loc'
		@location = ARGV[i+1]
	    when '-solr'
		@solr = "#{ARGV[i+1]}"
	    when '-solr-auth'
		@solr_auth_header = "#{ARGV[i+1]}"
	    when '-h'
		STDERR.puts "use: ruby TimbuctooScraper.rb -loc location -solr solr-site [--debug]"
		exit(1)
	  end
	end
    rescue => detail
	STDERR.puts "#{detail}"
    end

    continu = true
    start_value = 0
    num_of_lines = 100 # Persons.debug ? 10 : 100

end

