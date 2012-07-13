

var questions = function($, _, Backbone) {


    // Collection
//    var questionList;

//    var _product_id = Math.floor(Math.random()*100000000000);
    var _product_id = "foo";
    var _hostname = "heman";

    // Backbone settings
    Backbone.emulateHTTP = true;

    // XHR mappings
    $.ajaxSetup({ cache: false });

    // Question url mapping
//    var xhrMap = {

        //"create"    : {"GET_override": true, "uri" : "http://"+_hostname+":10090/question/for/"+_product_id},    // Model.save() - only if model.isNew()
        //"read"      : {"GET_override": true, "uri" : "http://"+_hostname+":10090/question/1342084417319"}//,   // Model.fetch() hostname/answer/for/<questionid>
        // "update"    : {"GET_override": true, "uri" : "http://heman:10090/question/for/1234"},    // Model.save()
        // "delete"    : {"GET_override": true, "uri" : "http://heman:10090/question/for/1234"}     // Model.destroy()
//    };


    // Template Settings for Underscore
    var templateDelimiters = {
        interpolate   : /\{%(.+?)%\}/gim,       //eg: {% var %}
        escape        : /\{\\-(.+?)\\\}/gim,    //eg: {\ var with html escaping \}
        evaluate      : /\{=(.+?)=\}/gim        //eg: {= print(var || undefined) =}
    };


    // Model for Question(s)
    // @attributes: _id, text
    var Question = Backbone.Model.extend({

        idAttribute: "_id", // http://stackoverflow.com/questions/10874202/why-does-backbone-not-send-the-delete

        initialize : function(){
        	console.info("new Question initialized");
            var $this = this;

            if( !this.get('answers') ){
                this.set({answers: new Array()});
            }

            this.fetch();
            setInterval(function(){ $this.fetch() }, 2000);

            return this;
        },

        // this is a workaround for emulateHTTP url mapping (see sync method below)
        // for when a call needs url params attached as querystring; awkward = true
//        attributes_to_querystring : function() {
//            var q = "";
//            _.each(this.attributes, function(attr, key){
//                q = q + "&" + encodeURIComponent(key) + "=" + encodeURIComponent(attr);
//            },
//            this);
//            return q;
//        },

        // overwrite sync() for method and url mapping
        // the methods are all POSTS because Backbone.emulateHTTP is set to true above
        // on the server side, the webservice needs to be able to handle the json blobs that Backbone sends out as POST data
        sync : function(method, model, options) {

            options = options || {};
            options.url = "http://"+_hostname+":10090/answer/list/"+this.attributes["_id"];  //hostname/answer/list/<questionid>

           console.log("Question.sync() \n@param method:", JSON.stringify(method), "\n@param model:", JSON.stringify(model), "; \n@param options: ", JSON.stringify(options));
           Backbone.sync(method, model, options);
        },

        parse : function(response) {
//            this.attributes["answers"] = response;
            this.set({answers : response});
            console.log("model parse()",this.attributes['answers']);
//            return response;
        }

    });


    // Collection of Actions
//    var QuestionList = Backbone.Collection.extend({
//
//        model       : Question,
//        initialize  : function(){
//			/* console.log("new ActionList initialize()"); */
//			// this.fetch();
//		},
//        // comparator  : function(action){ return action.get("points") },
//        url         : function(){ return xhrMap['read'].uri },
//        parse       : function(response){ return response }
//
//    });


    // View Controller for Actions
    var QuestionView = Backbone.View.extend({

        template : function(data) {

            // nest data object inside of another object so template won't barf on missing data values
            // this means template must refer to variables as {% data.label %} for example
            // https://github.com/documentcloud/underscore/issues/237
//            console.log("template data:", data);

            var _template_data = {};
                _template_data.data = data;

            return _.template( $("#question-display-template").html(), _template_data, templateDelimiters )
        },

        events : {
			// 'submit form#question-form' : 'fetch_collection',
			// 'click a#question-submit'   : 'save_cua'
        },

        initialize : function() {
           console.log("new UserActionView initialized; this.model:", this.model);
            var $this = this;

            _.bindAll(this,  'render'
                            ,'collection_reset'
                            ,'append_to_list'
            );

            if (!this.model) throw "Error in QuestionView: model undefined";

            // create ul
            console.log("view initialize() attr id:",this.model.attributes["_id"]);
            var ul_id = "question-" + this.model.attributes["_id"];
            $("#question-display-ul").append("<ul id='" + ul_id + "'></ul>");

            // model event bindings
            this.model.bind('reset', this.collection_reset);

            setInterval(function(){ $this.render() }, 2000);
            return this;
        },

        render : function(){
           console.log("view render()", this.model);
            var $this = this;

            // empty out list before adding li's
//            console.info("render() attr id:",this.model.attributes["_id"]);
            var ul_id = "#question-" + this.model.attributes["_id"];
            $(this.el).find(ul_id).html("<li><h2>"+this.model.attributes["question"]+"</h2></li>");

            // each action to ul list on page
            console.info("render() attr answers:", this.model.attributes["answers"]);

            _(this.model.attributes["answers"]).each(function(answer){
                $this.append_to_list(answer);
            }, this);

            return this;
        },

        collection_reset : function(e) {
           console.log("model_change()", e);
            this.render();
            return this;
        },

        append_to_list : function(answer) {
            console.log("append_to_list()", answer);
            var $this = this;
            var html = this.template(answer);
//            console.info("append_to_list() append html:", html);
//            console.info("append_to_list() attr id:",this.model.attributes["_id"]);
            var ul_id = "#question-" + this.model.attributes["_id"];
//            console.info("append to ", ul_id);
            $(ul_id).append(html);

            return this;
        }
    });


    var init = (function(){


		// Backbone MVC Init
//		questionList = new QuestionList([]);

        var hack_questionIdsList = [];
		
		 // initialize view
//		var questionView = new QuestionView({
//			collection  : questionList,
//			el 		    : $("#question-container")
//		});

		// set up polling for questions & answers
		// setInterval(function () { questionList.fetch(); }, 5000);
		


        // temp init
        // init Question model
//        var question_atts = {
//            _id : "1342124523886"
//        };
//        var nq = new Question(question_atts);
//        nq.on("error",function(model, error){console.log("init question error", error)});
//
//        var questionView = new QuestionView({
//			model  : nq,
//			el 	   : $("#question-display-ul")
//		});



        // submit question flow
		$("#question-submit").click(function(e){
			e.preventDefault();
			submitQuestion();
            $("#ask-question").val("");
			// questionList.fetch();
		});
		$("#question-form").submit(function(e){
			e.preventDefault();
			submitQuestion();
            $("#ask-question").val("");
			// questionList.fetch();
		});
		
		function submitQuestion() {
			var q = {};
            q.text = $("#ask-question").val();
            q.rnum = Math.floor(Math.random()*1000000);
            drawQuestion(q);
            $.ajax({
                url: "http://"+_hostname+":10090/question/for/"+_product_id, // returns question id
                dataType: 'json',
                type: 'GET',
                timeout : 7000,
                data : q,
                success: function(data) {
                    console.log("ajax success response: ", data);
                    //console.log(q.rnum);
                    $('#rand-'+ q.rnum).attr("id","answers-for-"+data.ok);
                    hack_questionIdsList.push(data.ok);  // hostname/answer/for/<questionid>


                    // init Question model
                    var question_atts = {
                        _id : data.ok,
                        question : $("#ask-question").val()
                    };
                    var nq = new Question(question_atts);
                    nq.on("error",function(model, error){console.log("init question error", error)});

                    var questionView = new QuestionView({
                        model  : nq,
                        el 	   : $("#question-display-ul")
                    });


                },
                error: function() {
                    alert("json response error");
                }
            });
		}

        function drawQuestion(questionData) {
            var questionHtml = "" +
            "<ul class='question-display-ul'><li><h2 class='question-display-question-text'>"+questionData.text+"</h2><p class='question-display-question-asker'></p> <div id='rand-"+questionData.rnum+"'></div> </li></ul>";
            $('#question-display').html(questionHtml+$('#question-display').html());
        }
		
		// ask question character count
	    $("#ask-question").keyup(function(e){
				// logger.info("keyup", e);
				var t = $("#question-length");
				var c = 140 - $(e.target).val().length;
				t.text(c);
				if (c<0 && !t.hasClass("error")) {
					t.addClass("error");
				} else if (c>-1 && t.hasClass("error")) {
					t.removeClass("error");
				}
		});
		
//		console.info("init complete");
		
		
		// DEBUG
		
		// (function() {
		// 	        $.ajax({
		// 	            url: "http://heman:10090/question/1342084417319",
		// 		// url: 'http://where.yahooapis.com/v1/places.woeid(28656958)?appid=wTAykynV34EggVpX2AWJPYkAAyV7Hrdlcj0CSdF1glsVmbhQ45HgFarbM3QgZJmL&format=json',
		// 	            dataType: 'text',			
		// 	            type: 'GET',
		// 	            timeout : 7000,
		// 		// data : q,
		// 	            success: function(data) {
		// 	                console.log("ajax success: ", data);
		// 	            },
		// 	            error: function() {
		// 	                console.log("ajax fail");
		// 	            }
		// 	        });			
		// })();
		
    })();


}(jQuery, _, Backbone);
