

var questions = function($, _, Backbone) {


    // Collection
    var questionList;

    var _product_id = Math.floor(Math.random()*100000000000);
    var _hostname = "heman";
    // Backbone settings
    Backbone.emulateHTTP = true;


    // XHR mappings
    $.ajaxSetup({ cache: false });

    var xhrMap = {

        "create"    : {"GET_override": true, "uri" : "http://"+_hostname+":10090/question/for/"+_product_id},    // Model.save() - only if model.isNew()
        "read"      : {"GET_override": true, "uri" : "http://"+_hostname+":10090/question/1342084417319"},   // Model.fetch()
        // "update"    : {"GET_override": true, "uri" : "http://heman:10090/question/for/1234"},    // Model.save()
        // "delete"    : {"GET_override": true, "uri" : "http://heman:10090/question/for/1234"}     // Model.destroy()
    };


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
            return this;
        },

        // this is a workaround for emulateHTTP url mapping (see sync method below)
        // for when a call needs url params attached as querystring; awkward = true
        attributes_to_querystring : function() {
            var q = "";
            _.each(this.attributes, function(attr, key){
                q = q + "&" + encodeURIComponent(key) + "=" + encodeURIComponent(attr);
            },
            this);
            return q;
        },

        // overwrite sync() for method and url mapping
        // the methods are all POSTS because Backbone.emulateHTTP is set to true above
        // on the server side, the webservice needs to be able to handle the json blobs that Backbone sends out as POST data
        sync : function(method, model, options) {
            options = options || {};
            options.url = xhrMap[method.toLowerCase()].uri;

            // GET_override doesnt actually change method used to ping server, it just populates the model attributes in the querystring
            if (xhrMap[method.toLowerCase()].GET_override) { options.url = options.url + this.attributes_to_querystring(); }

           console.log("Action.sync() \n@param method:", JSON.stringify(method), "\n@param model:", JSON.stringify(model), "; \n@param options: ", JSON.stringify(options));
           Backbone.sync(method, model, options);
        }

    });


    // Collection of Actions
    var QuestionList = Backbone.Collection.extend({

        model       : Question,
        initialize  : function(){  
			/* console.log("new ActionList initialize()"); */ 
			// this.fetch();
		},
        // comparator  : function(action){ return action.get("points") },
        url         : function(){ return xhrMap['read'].uri },
        parse       : function(response){ return response }

    });


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
           console.log("new UserActionView initialized; this.collection:", this.collection);

            $this = this;
            _.bindAll(this,  'render'
                            ,'collection_reset'
                            ,'append_to_list'
            );

            if (!this.collection) throw "Error in UserActionView: collection undefined";

            // collection event bindings
            this.collection.bind('reset', this.collection_reset);

            this.render();

            return this;
        },

        render : function(){
           console.log("render()", this.collection.pluck("_id"));

            var $this = this;

            // hide dialog if open
            $(this.elDialog).hide();

            // empty out list before adding li's
            $(this.el).find("#pr-cua-list").html("");

            // each action to ul list on page
            _(this.collection.models).each(function(action){
//                console.log(JSON.stringify(action));
                $this.append_action_to_list(action);
            }, this);
            return this;
        },


        // Handlers for Collection Events
        collection_reset : function(e) {
           console.log("collection_change()", e);
            this.render();
            return this;
        },

        // Handlers for DOM Events
        append_to_list : function(action) {
            var cid = action.cid;
            action = action.toJSON();
            action.cid = cid;
            console.log("append_to_list()", action);
            var html = this.template(action);
            $(this.el).find("#question-display-ul").append(html);
            return this;
        }
    });


    var init = (function(){


		// Backbone MVC Init
		questionList = new QuestionList([]);

        var hack_questionIdsList = [];
		
		 // initialize view
		var questionView = new QuestionView({
			collection  : questionList,
			el 		    : $("#question-container")
		});

		// set up polling for questions & answers
		// setInterval(function () { questionList.fetch(); }, 5000);
		
		
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
                url: "http://"+_hostname+":10090/question/for/"+_product_id,
                dataType: 'json',
                type: 'GET',
                timeout : 7000,
                data : q,
                success: function(data) {
                    console.log("ajax success: ", data);
                    console.log(q.rnum);
                    $('#rand-'+ q.rnum).attr("id","answers-for-"+data.ok);
                    hack_questionIdsList.push(data.ok);
                },
                error: function() {
                    alert("json response error");
                }
            });
		}

        function drawQuestion(questionData) {
            var questionHtml = "" +
    "<ul class='question-display-ul'><li><h2 class='question-display-question-text'>"+questionData.text+"</h2><p class='question-display-question-asker'>-- asked by ???</p> <div id='rand-"+questionData.rnum+"'></div> </li></ul>";

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
		
		console.info("init complete");
		
		
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
