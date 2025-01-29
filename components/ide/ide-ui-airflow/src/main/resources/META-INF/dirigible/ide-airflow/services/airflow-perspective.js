const perspectiveData = {
	id: "airflow",
	name: "Airflow",
	link: "../ide-airflow/index.html",
	order: "101",
	image: "airflow", // deprecated
	icon: "../ide-airflow/images/airflow.svg",
};

if (typeof exports !== 'undefined') {
	exports.getPerspective = function () {
		return perspectiveData;
	}
}
